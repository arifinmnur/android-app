package com.kelsos.mbrc.content.radios

import com.kelsos.mbrc.di.modules.AppDispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RadioRepositoryImpl
@Inject constructor(
  private val localDataSource: LocalRadioDataSource,
  private val remoteDataSource: RemoteRadioDataSource,
  private val dispatchers: AppDispatchers
) : RadioRepository {
  override suspend fun getAllCursor(): List<RadioStation> =
    localDataSource.loadAllCursor()

  override suspend fun getAndSaveRemote(): List<RadioStation> {
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

  override suspend fun search(term: String): List<RadioStation> =
    localDataSource.search(term)

  override suspend fun cacheIsEmpty(): Boolean = localDataSource.isEmpty()

  override suspend fun count(): Long = localDataSource.count()
}
