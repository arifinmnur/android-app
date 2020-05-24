package com.kelsos.mbrc.features.radio.repository

import androidx.paging.DataSource
import arrow.core.Either
import com.kelsos.mbrc.common.data.Progress
import com.kelsos.mbrc.common.utilities.AppCoroutineDispatchers
import com.kelsos.mbrc.common.utilities.epoch
import com.kelsos.mbrc.features.radio.RadioDaoMapper
import com.kelsos.mbrc.features.radio.RadioDtoMapper
import com.kelsos.mbrc.features.radio.RadioStationDto
import com.kelsos.mbrc.features.radio.data.RadioStationDao
import com.kelsos.mbrc.features.radio.domain.RadioStation
import com.kelsos.mbrc.networking.ApiBase
import com.kelsos.mbrc.networking.protocol.Protocol
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.withContext

class RadioRepositoryImpl(
  private val dao: RadioStationDao,
  private val api: ApiBase,
  private val dispatchers: AppCoroutineDispatchers
) : RadioRepository {

  override suspend fun count(): Long = withContext(dispatchers.database) {
    dao.count()
  }

  override fun getAll(): DataSource.Factory<Int, RadioStation> {
    return dao.getAll().map { RadioDaoMapper.map(it) }
  }

  override suspend fun getRemote(progress: Progress): Either<Throwable, Unit> = Either.catch {
    return@catch withContext(dispatchers.network) {
      val added = epoch()
      val allPages = api.getAllPages(
        Protocol.RadioStations,
        RadioStationDto::class,
        progress
      )
      allPages.onCompletion {
        withContext(dispatchers.database) {
          dao.removePreviousEntries(added)
        }
      }.collect {
        val items = it.map { RadioDtoMapper.map(it).apply { dateAdded = added } }
        withContext(dispatchers.database) {
          withContext(dispatchers.database) {
            dao.insertAll(items)
          }
        }
      }
    }
  }

  override fun search(term: String): DataSource.Factory<Int, RadioStation> {
    return dao.search(term).map { RadioDaoMapper.map(it) }
  }

  override suspend fun cacheIsEmpty(): Boolean =
    withContext(dispatchers.database) { dao.count() == 0L }

  override suspend fun getById(id: Long): RadioStation? {
    return withContext(dispatchers.database) {
      val entity = dao.getById(id) ?: return@withContext null
      return@withContext RadioDaoMapper.map(entity)
    }
  }
}