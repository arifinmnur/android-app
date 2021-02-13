package com.kelsos.mbrc.ui.navigation.library.genreartists

import com.kelsos.mbrc.content.library.artists.ArtistEntity
import com.kelsos.mbrc.content.library.artists.ArtistRepository
import com.kelsos.mbrc.content.nowplaying.queue.Queue
import com.kelsos.mbrc.helper.QueueHandler
import com.kelsos.mbrc.mvp.BasePresenter
import com.kelsos.mbrc.utilities.paged
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
  override fun load(genre: String) {
    scope.launch {
      try {
        val data = repository.getArtistByGenre(genre)
        val liveData = data.paged()
        liveData.observe(this@GenreArtistsPresenterImpl, {
          if (it != null) {
            view().update(it)
          }
        })
      } catch (e: Exception) {
        Timber.v(e)
      }
    }
  }

  override fun queue(@Queue.Action action: String, entry: ArtistEntity) {
    scope.launch {
      val artist = entry.artist
      val (success, tracks) = queue.queueArtist(action, artist)
      view().queue(success, tracks)
    }
  }
}
