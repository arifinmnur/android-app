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

  override suspend fun getAll(): DataSource.Factory<Int, RadioStationEntity> =
    dao.getAll()

  override suspend fun getAndSaveRemote(): DataSource.Factory<Int, RadioStationEntity> {
    getRemote()
    return dao.getAll()
  }

  override suspend fun getRemote() {
    withContext(dispatchers.network) {
      val added = epoch()
      api.getAllPages(Protocol.RadioStations, RadioStationDto::class)
        .onCompletion {
          dao.removePreviousEntries(added)
        }.collect {
          dao.insertAll(it.map { mapper.map(it).apply { dateAdded = added } })
        }
    }
  }

  override suspend fun search(term: String): DataSource.Factory<Int, RadioStationEntity> =
    dao.search(term)

  override suspend fun cacheIsEmpty(): Boolean = dao.count() == 0L

  override suspend fun count(): Long = dao.count()
}