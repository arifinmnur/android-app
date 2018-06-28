package com.kelsos.mbrc.ui.navigation.library.genreartists

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.kelsos.mbrc.content.library.artists.ArtistEntity
import com.kelsos.mbrc.content.library.artists.ArtistRepository
import com.kelsos.mbrc.content.nowplaying.queue.LibraryPopup
import com.kelsos.mbrc.helper.QueueHandler
import com.kelsos.mbrc.mvp.BasePresenter
import com.kelsos.mbrc.utilities.paged
import kotlinx.coroutines.launch
import timber.log.Timber

class GenreArtistsPresenterImpl(
  private val repository: ArtistRepository,
  private val queue: QueueHandler
) : BasePresenter<GenreArtistsView>(), GenreArtistsPresenter {

  private lateinit var artists: LiveData<PagedList<ArtistEntity>>

  override fun load(genre: String) {
    scope.launch {
      try {
        artists = repository.getArtistByGenre(genre).paged()
        artists.observe(this@GenreArtistsPresenterImpl, {
          if (it != null) {
            view().update(it)
          }
        })
      } catch (e: Exception) {
        Timber.v(e)
      }
    }
  }

  override fun queue(@LibraryPopup.Action action: String, entry: ArtistEntity) {
    scope.launch {
      val artist = entry.artist
      val (success, tracks) = queue.queueArtist(action, artist)
      view().queue(success, tracks)
    }
  }
}