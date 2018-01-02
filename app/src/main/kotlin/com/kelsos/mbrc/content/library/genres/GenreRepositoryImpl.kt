package com.kelsos.mbrc.content.library.genres

import androidx.lifecycle.LiveData
import com.kelsos.mbrc.di.modules.AppDispatchers
import com.kelsos.mbrc.utilities.epoch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
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

  override suspend fun getAll(): LiveData<List<GenreEntity>> = dao.getAll()

  override suspend fun getAndSaveRemote(): LiveData<List<GenreEntity>> {
    getRemote()
    return dao.getAll()
  }

  override suspend fun getRemote() {
    val added = epoch()
    withContext(dispatchers.io) {
      remoteDataSource.fetch()
        .onCompletion {
          dao.removePreviousEntries(added)
        }
        .collect {
          dao.saveAll(it.map { mapper.map(it).apply { dateAdded = added } })
        }
    }
  }

  override suspend fun search(term: String): LiveData<List<GenreEntity>> = dao.search(term)

  override suspend fun cacheIsEmpty(): Boolean = dao.count() == 0L

  override suspend fun count(): Long = dao.count()
}
