package com.kelsos.mbrc.content.nowplaying

import com.kelsos.mbrc.di.modules.AppDispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import javax.inject.Inject

class NowPlayingRepositoryImpl
@Inject constructor(
  private val remoteDataSource: RemoteNowPlayingDataSource,
  private val localDataSource: LocalNowPlayingDataSource,
  private val dispatchers: AppDispatchers
) : NowPlayingRepository {
  override suspend fun getAllCursor(): List<NowPlaying> = localDataSource.loadAllCursor()

  override suspend fun getAndSaveRemote(): List<NowPlaying> {
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

  override suspend fun search(term: String): List<NowPlaying> =
    localDataSource.search(term)

  override suspend fun cacheIsEmpty(): Boolean = localDataSource.isEmpty()

  override suspend fun count(): Long = localDataSource.count()
}
