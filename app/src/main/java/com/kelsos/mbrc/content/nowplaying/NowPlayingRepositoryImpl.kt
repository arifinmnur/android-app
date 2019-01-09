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
  private val api: ApiBase,
  private val dao: NowPlayingDao,
  private val dispatchers: AppCoroutineDispatchers
) : NowPlayingRepository {
  private val mapper = NowPlayingDtoMapper()

  override suspend fun count(): Long = withContext(dispatchers.database) { dao.count() }

  override fun getAll(): DataSource.Factory<Int, NowPlayingEntity> = dao.getAll()

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

  override fun search(term: String): DataSource.Factory<Int, NowPlayingEntity> = dao.search(term)

  override suspend fun cacheIsEmpty(): Boolean =
    withContext(dispatchers.database) { dao.count() == 0L }

  override fun move(from: Int, to: Int) {
    TODO("implement move")
  }
}