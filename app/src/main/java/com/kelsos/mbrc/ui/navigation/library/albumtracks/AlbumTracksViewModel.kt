package com.kelsos.mbrc.ui.navigation.library.albumtracks

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import com.kelsos.mbrc.content.library.albums.AlbumInfo
import com.kelsos.mbrc.content.library.tracks.Track
import com.kelsos.mbrc.content.library.tracks.TrackRepository
import com.kelsos.mbrc.di.modules.AppCoroutineDispatchers
import com.kelsos.mbrc.utilities.paged
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AlbumTracksViewModel(
  private val repository: TrackRepository,
  private val dispatchers: AppCoroutineDispatchers
) : ViewModel() {
  private val job: Job = Job()
  private val scope = CoroutineScope(dispatchers.database + job)

  private lateinit var tracks: LiveData<PagedList<Track>>

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