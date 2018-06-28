package com.kelsos.mbrc.ui.navigation.library.artists

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.paging.PagedList
import com.kelsos.mbrc.content.library.artists.ArtistEntity
import com.kelsos.mbrc.content.library.artists.ArtistRepository
import com.kelsos.mbrc.content.sync.LibrarySyncUseCase
import com.kelsos.mbrc.helper.QueueHandler
import com.kelsos.mbrc.mvp.BasePresenter
import com.kelsos.mbrc.preferences.SettingsManager
import com.kelsos.mbrc.ui.navigation.library.LibrarySearchModel
import com.kelsos.mbrc.utilities.paged
import kotlinx.coroutines.launch
import timber.log.Timber

class BrowseArtistPresenterImpl(
  private val repository: ArtistRepository,
  private val settingsManager: SettingsManager,
  private val syncUseCase: LibrarySyncUseCase,
  private val queue: QueueHandler,
  private val searchModel: LibrarySearchModel
) : BasePresenter<BrowseArtistView>(),
  BrowseArtistPresenter {

  private lateinit var artists: LiveData<PagedList<ArtistEntity>>

  init {
    searchModel.term.observe(this) { term -> updateUi(term) }
  }

  override fun load() {
    updateUi(searchModel.term.value ?: "")
  }

  private fun updateUi(term: String) {
    scope.launch {
      view().search(term)
      try {
        onArtistsLoaded(getData(term))
      } catch (e: Exception) {
        Timber.v(e, "Error while loading the data from the database")
      }
      view().hideLoading()
    }
  }

  private fun onArtistsLoaded(factory: DataSource.Factory<Int, ArtistEntity>) {
    if (::artists.isInitialized) {
      artists.removeObservers(this)
    }

    artists = factory.paged()
    artists.observe(this@BrowseArtistPresenterImpl, {
      if (it != null) {
        view().update(it)
      }
    })
  }

  private suspend fun getData(term: String): DataSource.Factory<Int, ArtistEntity> {
    return if (term.isEmpty()) {
      val shouldDisplay = settingsManager.shouldDisplayOnlyAlbumArtists()
      if (shouldDisplay) {
        repository.getAlbumArtistsOnly()
      } else {
        repository.getAll()
      }
    } else {
      repository.search(term)
    }
  }

  override fun sync() {
    scope.launch {
      syncUseCase.sync()
    }
  }

  override fun queue(action: String, entry: ArtistEntity) {
    scope.launch {
      val artist = entry.artist
      val (success, tracks) = queue.queueArtist(action, artist)
      view().queue(success, tracks)
    }
  }
}