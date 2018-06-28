package com.kelsos.mbrc.ui.navigation.library.artistalbums

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.kelsos.mbrc.content.library.albums.AlbumEntity
import com.kelsos.mbrc.content.library.albums.AlbumRepository
import com.kelsos.mbrc.helper.QueueHandler
import com.kelsos.mbrc.mvp.BasePresenter
import com.kelsos.mbrc.utilities.paged
import kotlinx.coroutines.launch
import timber.log.Timber

class ArtistAlbumsPresenterImpl(
  private val repository: AlbumRepository,
  private val queue: QueueHandler
) : BasePresenter<ArtistAlbumsView>(),
  ArtistAlbumsPresenter {

  private lateinit var albums: LiveData<PagedList<AlbumEntity>>

  override fun load(artist: String) {
    scope.launch {
      try {
        albums = repository.getAlbumsByArtist(artist).paged()
        albums.observe(this@ArtistAlbumsPresenterImpl, {
          if (it != null) {
            view().update(it)
          }
        })
      } catch (e: Exception) {
        Timber.v(e)
      }
    }
  }

  override fun queue(action: String, album: AlbumEntity) {
    scope.launch {
      val artist = album.artist
      val albumName = album.album
      val (success, tracks) = queue.queueAlbum(action, albumName, artist)
      view().queue(success, tracks)
    }
  }
}