package com.kelsos.mbrc.content.library.genres

import com.kelsos.mbrc.di.modules.AppDispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GenreRepositoryImpl
@Inject
constructor(
  private val remoteDataSource: RemoteGenreDataSource,
  private val localDataSource: LocalGenreDataSource,
  private val dispatchers: AppDispatchers
) : GenreRepository {

  override suspend fun getAllCursor(): List<Genre> = localDataSource.loadAllCursor()

  override suspend fun getAndSaveRemote(): List<Genre> {
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

  override suspend fun search(term: String): List<Genre> = localDataSource.search(term)

  override suspend fun cacheIsEmpty(): Boolean = localDataSource.isEmpty()

  override suspend fun count(): Long = localDataSource.count()
}
