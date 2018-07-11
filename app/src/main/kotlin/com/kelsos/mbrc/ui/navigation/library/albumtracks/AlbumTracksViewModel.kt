package com.kelsos.mbrc.ui.navigation.library.albumtracks

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import com.kelsos.mbrc.content.library.albums.AlbumInfo
import com.kelsos.mbrc.content.library.tracks.TrackEntity
import com.kelsos.mbrc.content.library.tracks.TrackRepository
import com.kelsos.mbrc.utilities.paged

class AlbumTracksViewModel(
  private val repository: TrackRepository
) : ViewModel() {

  private lateinit var tracks: LiveData<PagedList<TrackEntity>>

  fun load(album: AlbumInfo) {
    scope.launch {
      val data = if (album.album.isEmpty()) {
        repository.getNonAlbumTracks(album.artist)
      } else {
        repository.getAlbumTracks(album.album, album.artist)
      }
      tracks = data.paged()
    }
  }
}