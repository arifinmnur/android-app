package com.kelsos.mbrc.content.playlists

import com.kelsos.mbrc.di.modules.AppDispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PlaylistRepositoryImpl
@Inject constructor(
  private val localDataSource: LocalPlaylistDataSource,
  private val remoteDataSource: RemotePlaylistDataSource,
  private val dispatchers: AppDispatchers
) : PlaylistRepository {
  override suspend fun getAllCursor(): List<Playlist> = localDataSource.loadAllCursor()

  override suspend fun getAndSaveRemote(): List<Playlist> {
    getRemote()
    return localDataSource.loadAllCursor()
  }

  override suspend fun getRemote() {
    localDataSource.deleteAll()
    withContext(dispatchers.io) {
      remoteDataSource.fetch().collect {
        localDataSource.saveAll(it)
      }
    }
  }

  override suspend fun search(term: String): List<Playlist> = localDataSource.search(term)

  override suspend fun cacheIsEmpty(): Boolean = localDataSource.isEmpty()

  override suspend fun count(): Long = localDataSource.count()
}
