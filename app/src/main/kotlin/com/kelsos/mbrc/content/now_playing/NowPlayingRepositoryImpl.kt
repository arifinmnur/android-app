package com.kelsos.mbrc.content.now_playing

import com.kelsos.mbrc.di.modules.AppDispatchers
import com.raizlabs.android.dbflow.list.FlowCursorList
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import javax.inject.Inject

class NowPlayingRepositoryImpl
@Inject constructor(
  private val remoteDataSource: RemoteNowPlayingDataSource,
  private val localDataSource: LocalNowPlayingDataSource,
  private val dispatchers: AppDispatchers
) : NowPlayingRepository {
  override suspend fun getAllCursor(): FlowCursorList<NowPlaying> = localDataSource.loadAllCursor()

  override suspend fun getAndSaveRemote(): FlowCursorList<NowPlaying> {
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

  override suspend fun search(term: String): FlowCursorList<NowPlaying> =
    localDataSource.search(term)

  override suspend fun cacheIsEmpty(): Boolean = localDataSource.isEmpty()

  override suspend fun count(): Long = localDataSource.count()
}
