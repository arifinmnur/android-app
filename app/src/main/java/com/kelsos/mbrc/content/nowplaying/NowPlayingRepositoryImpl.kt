package com.kelsos.mbrc.content.nowplaying

import androidx.paging.DataSource
import arrow.core.Try
import com.kelsos.mbrc.di.modules.AppCoroutineDispatchers
import com.kelsos.mbrc.networking.ApiBase
import com.kelsos.mbrc.networking.protocol.Protocol
import com.kelsos.mbrc.utilities.epoch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.withContext

class NowPlayingRepositoryImpl(
  private val api: ApiBase,
  private val dao: NowPlayingDao,
  private val dispatchers: AppCoroutineDispatchers
) : NowPlayingRepository {
  private val mapper = NowPlayingDtoMapper()
  private val entity2model = NowPlayingEntityMapper()

  override suspend fun count(): Long = withContext(dispatchers.database) { dao.count() }

  override fun getAll(): DataSource.Factory<Int, NowPlaying> {
    return dao.getAll().map { entity2model.map(it) }
  }

  override suspend fun getRemote(): Try<Unit> = Try {
    val added = epoch()
    withContext(dispatchers.network) {
      api.getAllPages(Protocol.NowPlayingList, NowPlayingDto::class)
        .onCompletion {
          withContext(dispatchers.database) {
            dao.removePreviousEntries(added)
          }
        }
        .collect { item ->
          val list = item.map { mapper.map(it).apply { dateAdded = added } }
          withContext(dispatchers.database) {
            dao.insertAll(list)
          }
        }
    }
  }

  override fun search(term: String): DataSource.Factory<Int, NowPlaying> =
    dao.search(term).map { entity2model.map(it) }

  override suspend fun cacheIsEmpty(): Boolean =
    withContext(dispatchers.database) {
    dao.count() == 0L
  }

  override fun move(from: Int, to: Int) {
    TODO("implement move")
  }
}