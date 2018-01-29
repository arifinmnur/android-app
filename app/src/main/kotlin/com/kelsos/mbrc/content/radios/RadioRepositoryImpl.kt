package com.kelsos.mbrc.content.radios

import androidx.paging.DataSource
import com.kelsos.mbrc.di.modules.AppDispatchers
import com.kelsos.mbrc.utilities.epoch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RadioRepositoryImpl
@Inject
constructor(
  private val dao: RadioStationDao,
  private val remoteDataSource: RemoteRadioDataSource,
  private val dispatchers: AppDispatchers
) : RadioRepository {
  private val mapper = RadioDtoMapper()

  override suspend fun getAll(): DataSource.Factory<Int, RadioStationEntity> =
    dao.getAll()

  override suspend fun getAndSaveRemote(): DataSource.Factory<Int, RadioStationEntity> {
    getRemote()
    return dao.getAll()
  }

  override suspend fun getRemote() {
    withContext(dispatchers.io) {
      val added = epoch()
      remoteDataSource.fetch()
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
