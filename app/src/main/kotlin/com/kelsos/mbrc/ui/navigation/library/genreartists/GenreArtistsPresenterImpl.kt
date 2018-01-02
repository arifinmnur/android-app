package com.kelsos.mbrc.ui.navigation.library.genreartists

import androidx.lifecycle.LiveData
import com.kelsos.mbrc.content.library.artists.ArtistEntity
import com.kelsos.mbrc.content.library.artists.ArtistRepository
import com.kelsos.mbrc.content.nowplaying.queue.LibraryPopup
import com.kelsos.mbrc.helper.QueueHandler
import com.kelsos.mbrc.mvp.BasePresenter
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class GenreArtistsPresenterImpl
@Inject
constructor(
  private val repository: ArtistRepository,
  private val queue: QueueHandler
) : BasePresenter<GenreArtistsView>(),
  GenreArtistsPresenter {

  private lateinit var artists: LiveData<List<ArtistEntity>>

  override fun load(genre: String) {
    scope.launch {
      try {
        artists = repository.getArtistByGenre(genre)
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
