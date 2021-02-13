package com.kelsos.mbrc.ui.navigation.library.artist_albums

import com.kelsos.mbrc.content.library.albums.Album
import com.kelsos.mbrc.helper.QueueHandler
import com.kelsos.mbrc.content.library.albums.AlbumRepository
import com.kelsos.mbrc.mvp.BasePresenter
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class ArtistAlbumsPresenterImpl
@Inject
constructor(
  private val repository: AlbumRepository,
  private val queue: QueueHandler
) : BasePresenter<ArtistAlbumsView>(),
  ArtistAlbumsPresenter {
  override fun load(artist: String) {
    scope.launch {
      try {
        view?.update(repository.getAlbumsByArtist(artist))
      } catch (e: Exception) {
        Timber.v(e)
      }
    }
  }

  override fun queue(action: String, album: Album) {
    scope.launch {
      val artist = album.artist ?: throw IllegalArgumentException("artist is null")
      val albumName = album.album ?: throw java.lang.IllegalArgumentException("album is null")
      val (success, tracks) = queue.queueAlbum(action, albumName, artist)
      view?.queue(success, tracks)
    }
  }
}
