package com.kelsos.mbrc.now_playing

import com.raizlabs.android.dbflow.list.FlowCursorList
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class NowPlayingRepositoryImpl
@Inject constructor(private val remoteDataSource: RemoteNowPlayingDataSource,
                    private val localDataSource: LocalNowPlayingDataSource) : NowPlayingRepository {
  override fun getAllCursor(): Single<FlowCursorList<NowPlaying>> {
    return localDataSource.loadAllCursor().firstOrError()
  }

  override fun getAndSaveRemote(): Single<FlowCursorList<NowPlaying>> {
    return getRemote().andThen(localDataSource.loadAllCursor().firstOrError())
  }

  override fun getRemote(): Completable {
    localDataSource.deleteAll()
    return remoteDataSource.fetch().doOnNext {
      localDataSource.saveAll(it)
    }.ignoreElements()
  }

  override fun search(term: String): Single<FlowCursorList<NowPlaying>> {
    return localDataSource.search(term)
  }

  override fun cacheIsEmpty(): Single<Boolean> {
    return localDataSource.isEmpty()
  }
}