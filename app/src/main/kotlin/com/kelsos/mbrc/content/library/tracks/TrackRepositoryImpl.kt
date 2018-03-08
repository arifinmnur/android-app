package com.kelsos.mbrc.content.library.tracks

import androidx.paging.DataSource
import com.kelsos.mbrc.di.modules.AppDispatchers
import com.kelsos.mbrc.utilities.epoch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TrackRepositoryImpl
@Inject constructor(
  private val dao: TrackDao,
  private val remoteDataSource: RemoteTrackDataSource,
  private val dispatchers: AppDispatchers
) : TrackRepository {

  private val mapper = TrackDtoMapper()

  override suspend fun getAll(): DataSource.Factory<Int, TrackEntity> = dao.getAll()

  override suspend fun getAlbumTracks(
    album: String,
    artist: String
  ): DataSource.Factory<Int, TrackEntity> =
    dao.getAlbumTracks(album, artist)

  override suspend fun getNonAlbumTracks(artist: String): DataSource.Factory<Int, TrackEntity> =
    dao.getNonAlbumTracks(artist)

  override suspend fun getAndSaveRemote(): DataSource.Factory<Int, TrackEntity> {
    getRemote()
    return dao.getAll()
  }

  override suspend fun getRemote() {
    withContext(dispatchers.io) {
      val added = epoch()
      remoteDataSource.fetch().onCompletion {
        dao.removePreviousEntries(added)
      }.collect { items ->
        val tracks = items.map { mapper.map(it).apply { dateAdded = added } }
        dao.insertAll(tracks)
      }
    }
  }

  override suspend fun search(term: String): DataSource.Factory<Int, TrackEntity> {
    return dao.search(term)
  }

  override suspend fun getGenreTrackPaths(genre: String): List<String> {
    return dao.getGenreTrackPaths(genre)
  }

  override suspend fun getArtistTrackPaths(artist: String): List<String> =
    dao.getArtistTrackPaths(artist)

  override suspend fun getAlbumTrackPaths(album: String, artist: String): List<String> =
    dao.getAlbumTrackPaths(album, artist)

  override suspend fun getAllTrackPaths(): List<String> = dao.getAllTrackPaths()

  override suspend fun cacheIsEmpty(): Boolean = dao.count() == 0L

  override suspend fun count(): Long = dao.count()
}