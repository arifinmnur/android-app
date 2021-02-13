package com.kelsos.mbrc.library.tracks

import com.kelsos.mbrc.repository.Repository
import com.raizlabs.android.dbflow.list.FlowCursorList

interface TrackRepository : Repository<Track> {
  suspend fun getAlbumTracks(album: String, artist: String): FlowCursorList<Track>
  suspend fun getNonAlbumTracks(artist: String): FlowCursorList<Track>
  suspend fun getAlbumTrackPaths(album: String, artist: String): List<String>
  suspend fun getGenreTrackPaths(genre: String): List<String>
  suspend fun getArtistTrackPaths(artist: String): List<String>
  suspend fun getAllTrackPaths(): List<String>
}
