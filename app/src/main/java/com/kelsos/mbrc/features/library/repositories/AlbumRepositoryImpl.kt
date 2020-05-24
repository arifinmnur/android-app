package com.kelsos.mbrc.features.library.repositories

import androidx.paging.DataSource
import arrow.core.Either
import com.kelsos.mbrc.common.data.Progress
import com.kelsos.mbrc.common.utilities.AppCoroutineDispatchers
import com.kelsos.mbrc.common.utilities.epoch
import com.kelsos.mbrc.features.library.data.Album
import com.kelsos.mbrc.features.library.data.AlbumDao
import com.kelsos.mbrc.features.library.data.AlbumEntityMapper
import com.kelsos.mbrc.features.library.dto.AlbumDto
import com.kelsos.mbrc.features.library.dto.AlbumDtoMapper
import com.kelsos.mbrc.networking.ApiBase
import com.kelsos.mbrc.networking.protocol.Protocol
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.withContext

class AlbumRepositoryImpl(
  private val dao: AlbumDao,
  private val api: ApiBase,
  private val dispatchers: AppCoroutineDispatchers
) : AlbumRepository {
  private val dtoMapper = AlbumDtoMapper()
  private val entityMapper = AlbumEntityMapper()

  override suspend fun count(): Long = withContext(dispatchers.database) { dao.count() }

  override fun getAlbumsByArtist(artist: String): DataSource.Factory<Int, Album> =
    dao.getAlbumsByArtist(artist).map { entityMapper.map(it) }

  override fun getAll(): DataSource.Factory<Int, Album> =
    dao.getAll().map { entityMapper.map(it) }

  override suspend fun getRemote(progress: Progress): Either<Throwable, Unit> = Either.catch {
    return@catch withContext(dispatchers.network) {
      val added = epoch()
      val allPages = api.getAllPages(
        Protocol.LibraryBrowseAlbums,
        AlbumDto::class,
        progress
      )

      allPages.onCompletion {
          withContext(dispatchers.database) {
            dao.removePreviousEntries(added)
          }
        }
        .collect {
          withContext(dispatchers.database) {
            dao.insert(it.map { dtoMapper.map(it) })
          }
        }
    }
  }

  override fun search(term: String): DataSource.Factory<Int, Album> =
    dao.search(term).map { entityMapper.map(it) }

  override suspend fun cacheIsEmpty(): Boolean =
    withContext(dispatchers.database) { dao.count() == 0L }

  override suspend fun getById(id: Long): Album? {
    return withContext(dispatchers.database) {
      val entity = dao.getById(id) ?: return@withContext null
      return@withContext entityMapper.map(entity)
    }
  }
}