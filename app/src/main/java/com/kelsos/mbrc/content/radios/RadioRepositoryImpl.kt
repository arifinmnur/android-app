package com.kelsos.mbrc.content.radios

import androidx.paging.DataSource
import com.kelsos.mbrc.di.modules.AppCoroutineDispatchers
import com.kelsos.mbrc.networking.ApiBase
import com.kelsos.mbrc.networking.protocol.Protocol
import com.kelsos.mbrc.utilities.epoch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.withContext

class RadioRepositoryImpl(
  private val dao: RadioStationDao,
  private val api: ApiBase,
  private val dispatchers: AppCoroutineDispatchers
) : RadioRepository {
  private val mapper = RadioDtoMapper()
  private val dao2Model = RadioDaoMapper()

  override suspend fun count(): Long = withContext(dispatchers.database) { dao.count() }

  override fun getAll(): DataSource.Factory<Int, RadioStation> {
    return dao.getAll().map { dao2Model.map(it) }
  }

  override suspend fun getRemote() {
    withContext(dispatchers.network) {
      val added = epoch()
      api.getAllPages(Protocol.RadioStations, RadioStationDto::class)
        .onCompletion {
          dao.removePreviousEntries(added)
        }.collect {
          val items = it.map { mapper.map(it).apply { dateAdded = added } }
          withContext(dispatchers.database) {
            dao.insertAll(items)
          }
        }
    }
  }

  override fun search(term: String): DataSource.Factory<Int, RadioStation> {
    return dao.search(term).map { dao2Model.map(it) }
  }

  override suspend fun cacheIsEmpty(): Boolean =
    withContext(dispatchers.database) { dao.count() == 0L }
}