package com.kelsos.mbrc.ui.navigation.library.genres

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.paging.PagedList
import com.kelsos.mbrc.content.library.genres.GenreEntity
import com.kelsos.mbrc.content.library.genres.GenreRepository
import com.kelsos.mbrc.content.sync.LibrarySyncInteractor
import com.kelsos.mbrc.helper.QueueHandler
import com.kelsos.mbrc.mvp.BasePresenter
import com.kelsos.mbrc.ui.navigation.library.LibrarySearchModel
import com.kelsos.mbrc.utilities.paged
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class BrowseGenrePresenterImpl
@Inject
constructor(
  private val repository: GenreRepository,
  private val librarySyncInteractor: LibrarySyncInteractor,
  private val queue: QueueHandler,
  private val searchModel: LibrarySearchModel
) : BasePresenter<BrowseGenreView>(), BrowseGenrePresenter {

  private lateinit var genres: LiveData<PagedList<GenreEntity>>

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
        onGenresLoaded(getData(term))
      } catch (e: Exception) {
        Timber.v(e, "Error while loading the data from the database")
      }
      view().hideLoading()
    }
  }

  private suspend fun getData(term: String): DataSource.Factory<Int, GenreEntity> {
    return if (term.isEmpty()) {
      repository.getAll()
    } else {
      repository.search(term)
    }
  }

  private fun onGenresLoaded(data: DataSource.Factory<Int, GenreEntity>) {
    if (::genres.isInitialized) {
      genres.removeObservers(this)
    }

    genres = data.paged()
    genres.observe(this@BrowseGenrePresenterImpl, {
      if (it != null) {
        view().update(it)
      }
    })
  }

  override fun sync() {
    scope.launch {
      librarySyncInteractor.sync()
    }
  }

  override fun queue(action: String, genre: GenreEntity) {
    scope.launch {
      val genreName = genre.genre
      val (success, tracks) = queue.queueGenre(action, genreName)
      view().queue(success, tracks)
    }
  }
}