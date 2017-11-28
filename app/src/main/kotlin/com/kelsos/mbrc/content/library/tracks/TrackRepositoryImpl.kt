package com.kelsos.mbrc.content.library.tracks

import com.kelsos.mbrc.content.library.UpdatedDataSource
import com.kelsos.mbrc.di.modules.AppDispatchers
import com.raizlabs.android.dbflow.list.FlowCursorList
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.withContext
import org.threeten.bp.Instant
import javax.inject.Inject

class TrackRepositoryImpl
@Inject constructor(
  private val localDataSource: LocalTrackDataSource,
  private val remoteDataSource: RemoteTrackDataSource,
  private val updatedDataSource: UpdatedDataSource,
  private val dispatchers: AppDispatchers
) : TrackRepository {

  override suspend fun getAllCursor(): FlowCursorList<Track> = localDataSource.loadAllCursor()

  override suspend fun getAlbumTracks(album: String, artist: String): FlowCursorList<Track> =
    localDataSource.getAlbumTracks(album, artist)

  override suspend fun getNonAlbumTracks(artist: String): FlowCursorList<Track> =
    localDataSource.getNonAlbumTracks(artist)

  override suspend fun getAndSaveRemote(): FlowCursorList<Track> {
    getRemote()
    return localDataSource.loadAllCursor()
  }

  override suspend fun getRemote() {
    val epoch = Instant.now().epochSecond

    localDataSource.deleteAll()
    withContext(dispatchers.io) {
      remoteDataSource.fetch().onCompletion {
        val paths = updatedDataSource.getPathInsertedAtEpoch(epoch)
        localDataSource.deletePaths(paths)
        updatedDataSource.deleteAll()
      }.collect { items ->
        localDataSource.saveAll(items)
        updatedDataSource.addUpdated(items.mapNotNull { it.src }, epoch)
      }
    }
  }

  override suspend fun search(term: String): FlowCursorList<Track> {
    return localDataSource.search(term)
  }

  override suspend fun getGenreTrackPaths(genre: String): List<String> {
    return localDataSource.getGenreTrackPaths(genre)
  }

  override suspend fun getArtistTrackPaths(artist: String): List<String> =
    localDataSource.getArtistTrackPaths(artist)

  override suspend fun getAlbumTrackPaths(album: String, artist: String): List<String> =
    localDataSource.getAlbumTrackPaths(album, artist)

  override suspend fun getAllTrackPaths(): List<String> = localDataSource.getAllTrackPaths()

  override suspend fun cacheIsEmpty(): Boolean = localDataSource.isEmpty()

  override suspend fun count(): Long = localDataSource.count()
}
