package com.kelsos.mbrc.content.library.genres

import androidx.paging.DataSource
import com.kelsos.mbrc.di.modules.AppCoroutineDispatchers
import com.kelsos.mbrc.networking.ApiBase
import com.kelsos.mbrc.networking.protocol.Protocol
import com.kelsos.mbrc.utilities.epoch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.withContext

class GenreRepositoryImpl(
  private val api: ApiBase,
  private val dao: GenreDao,
  private val dispatchers: AppCoroutineDispatchers
) : GenreRepository {

  private val mapper = GenreDtoMapper()

  override suspend fun getAll(): DataSource.Factory<Int, GenreEntity> =
    withContext(dispatchers.database) { dao.getAll() }

  override suspend fun getRemote() {
    val added = epoch()
    withContext(dispatchers.network) {
      api.getAllPages(Protocol.LibraryBrowseGenres, GenreDto::class)
        .onCompletion {
          dao.removePreviousEntries(added)
        }
        .collect {
          dao.insertAll(it.map { mapper.map(it).apply { dateAdded = added } })
        }
    }
  }

  override suspend fun search(term: String): DataSource.Factory<Int, GenreEntity> =
    withContext(dispatchers.database) { dao.search(term) }

  override suspend fun cacheIsEmpty(): Boolean =
    withContext(dispatchers.database) { dao.count() == 0L }

  override suspend fun count(): Long = withContext(dispatchers.database) { dao.count() }
}