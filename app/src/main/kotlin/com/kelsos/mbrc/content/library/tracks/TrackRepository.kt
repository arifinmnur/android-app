package com.kelsos.mbrc.content.library.tracks

import androidx.lifecycle.LiveData
import com.kelsos.mbrc.interfaces.data.Repository

interface TrackRepository : Repository<TrackEntity> {
  suspend fun getAlbumTracks(album: String, artist: String): LiveData<List<TrackEntity>>
  suspend fun getNonAlbumTracks(artist: String): LiveData<List<TrackEntity>>
  suspend fun getAlbumTrackPaths(album: String, artist: String): List<String>
  suspend fun getGenreTrackPaths(genre: String): List<String>
  suspend fun getArtistTrackPaths(artist: String): List<String>
  suspend fun getAllTrackPaths(): List<String>
}
