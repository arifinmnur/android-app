package com.kelsos.mbrc.content.library.tracks

import androidx.paging.DataSource
import com.kelsos.mbrc.interfaces.data.Repository

interface TrackRepository : Repository<TrackEntity> {
  suspend fun getAlbumTracks(album: String, artist: String): DataSource.Factory<Int, TrackEntity>
  suspend fun getNonAlbumTracks(artist: String): DataSource.Factory<Int, TrackEntity>
  suspend fun getAlbumTrackPaths(album: String, artist: String): List<String>
  suspend fun getGenreTrackPaths(genre: String): List<String>
  suspend fun getArtistTrackPaths(artist: String): List<String>
  suspend fun getAllTrackPaths(): List<String>
}