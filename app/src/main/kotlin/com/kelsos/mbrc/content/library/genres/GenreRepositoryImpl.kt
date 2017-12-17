package com.kelsos.mbrc.content.library.genres

import androidx.paging.DataSource
import com.kelsos.mbrc.di.modules.AppDispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GenreRepositoryImpl
@Inject
constructor(
  private val dao: GenreDao,
  private val remoteDataSource: RemoteGenreDataSource,
  private val dispatchers: AppDispatchers
) : GenreRepository {

  private val mapper = GenreDtoMapper()

  override suspend fun getAll(): DataSource.Factory<Int, GenreEntity> = dao.getAll()

  override suspend fun getAndSaveRemote(): DataSource.Factory<Int, GenreEntity> {
    getRemote()
    return dao.getAll()
  }

  override suspend fun getRemote() {
    dao.deleteAll()
    withContext(dispatchers.io) {
      remoteDataSource.fetch().collect {
        dao.saveAll(it.map { mapper.map(it) })
      }
    }
  }

  override suspend fun search(term: String): DataSource.Factory<Int, GenreEntity> = dao.search(term)

  override suspend fun cacheIsEmpty(): Boolean = dao.count() == 0L

  override suspend fun count(): Long = dao.count()
}
