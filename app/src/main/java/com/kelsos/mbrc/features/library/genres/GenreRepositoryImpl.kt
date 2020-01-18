package com.kelsos.mbrc.features.library.genres

import androidx.paging.DataSource
import arrow.core.Try
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
  private val dao2Model = GenreEntityMapper()

  override suspend fun count(): Long = withContext(dispatchers.database) { dao.count() }

  override fun getAll(): DataSource.Factory<Int, Genre> = dao.getAll().map { dao2Model.map(it) }

  override suspend fun getRemote(): Try<Unit> = Try {
    val added = epoch()
    val stored = dao.genres().associate { it.genre to it.id }
    withContext(dispatchers.network) {
      api.getAllPages(Protocol.LibraryBrowseGenres, GenreDto::class)
        .onCompletion {
          withContext(dispatchers.database) {
            dao.removePreviousEntries(added)
          }
        }
        .collect { genres ->
          val items = genres.map {
            mapper.map(it).apply {
              dateAdded = added

              val id = stored[it.genre]
              if (id != null) {
                this.id = id
              }
            }
          }
          withContext(dispatchers.database) {
            dao.insertAll(items)
          }
        }
    }
  }

  override fun search(term: String): DataSource.Factory<Int, Genre> =
    dao.search(term).map { dao2Model.map(it) }

  override suspend fun cacheIsEmpty(): Boolean =
    withContext(dispatchers.database) { dao.count() == 0L }
}