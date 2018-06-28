package com.kelsos.mbrc.content.nowplaying

import androidx.paging.DataSource
import com.kelsos.mbrc.di.modules.AppCoroutineDispatchers
import com.kelsos.mbrc.networking.ApiBase
import com.kelsos.mbrc.networking.protocol.Protocol
import com.kelsos.mbrc.utilities.epoch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.withContext

class NowPlayingRepositoryImpl(
  private val dao: NowPlayingDao,
  private val api: ApiBase,
  private val dispatchers: AppCoroutineDispatchers
) : NowPlayingRepository {
  private val mapper = NowPlayingDtoMapper()

  override suspend fun getAll(): DataSource.Factory<Int, NowPlayingEntity> = dao.getAll()

  override suspend fun getAndSaveRemote(): DataSource.Factory<Int, NowPlayingEntity> {
    getRemote()
    return dao.getAll()
  }

  override suspend fun getRemote() {
    val added = epoch()
    withContext(dispatchers.network) {
      api.getAllPages(Protocol.NowPlayingList, NowPlayingDto::class)
        .onCompletion {
          dao.removePreviousEntries(added)
        }
        .collect { item ->
          val list = item.map { mapper.map(it).apply { dateAdded = added } }
          dao.insertAll(list)
        }
    }
  }

  override suspend fun search(term: String): DataSource.Factory<Int, NowPlayingEntity> =
    dao.search(term)

  override suspend fun cacheIsEmpty(): Boolean = dao.count() == 0L

  override suspend fun count(): Long = dao.count()

  override fun move(from: Int, to: Int) {
    TODO("implement move")
  }
}