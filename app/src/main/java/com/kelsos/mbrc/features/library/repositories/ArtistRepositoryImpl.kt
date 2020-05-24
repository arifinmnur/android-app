package com.kelsos.mbrc.features.library.repositories

import androidx.paging.DataSource
import arrow.core.Either
import com.kelsos.mbrc.common.data.Progress
import com.kelsos.mbrc.common.utilities.AppCoroutineDispatchers
import com.kelsos.mbrc.common.utilities.epoch
import com.kelsos.mbrc.features.library.data.Artist
import com.kelsos.mbrc.features.library.data.ArtistDao
import com.kelsos.mbrc.features.library.data.ArtistEntityMapper
import com.kelsos.mbrc.features.library.dto.ArtistDto
import com.kelsos.mbrc.features.library.dto.ArtistDtoMapper
import com.kelsos.mbrc.networking.ApiBase
import com.kelsos.mbrc.networking.protocol.Protocol
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.withContext

class ArtistRepositoryImpl(
  private val dao: ArtistDao,
  private val api: ApiBase,
  private val dispatchers: AppCoroutineDispatchers
) : ArtistRepository {

  private val dtoMapper = ArtistDtoMapper()
  private val entityMapper = ArtistEntityMapper()

  override suspend fun count(): Long = withContext(dispatchers.database) {
    dao.count()
  }

  override fun getArtistByGenre(genre: String): DataSource.Factory<Int, Artist> =
    dao.getArtistByGenre(genre).map { entityMapper.map(it) }

  override fun getAll(): DataSource.Factory<Int, Artist> = dao.getAll().map { entityMapper.map(it) }

  override suspend fun getRemote(progress: Progress): Either<Throwable, Unit> = Either.catch {
    withContext(dispatchers.network) {
      val added = epoch()
      val allPages = api.getAllPages(
        Protocol.LibraryBrowseArtists,
        ArtistDto::class,
        progress
      )

      allPages.onCompletion {
        withContext(dispatchers.database) {
          dao.removePreviousEntries(added)
        }
      }
        .collect {
          val items = it.map { dtoMapper.map(it).apply { dateAdded = added } }
          withContext(dispatchers.database) {
            dao.insertAll(items)
          }
        }
    }
  }

  override fun search(term: String): DataSource.Factory<Int, Artist> =
    dao.search(term).map { entityMapper.map(it) }

  override fun getAlbumArtistsOnly(): DataSource.Factory<Int, Artist> =
    dao.getAlbumArtists().map { entityMapper.map(it) }

  override suspend fun cacheIsEmpty(): Boolean = withContext(dispatchers.database) {
    dao.count() == 0L
  }

  override suspend fun getById(id: Long): Artist? {
    return withContext(dispatchers.database) {
      val entity = dao.getById(id) ?: return@withContext null
      return@withContext entityMapper.map(entity)
    }
  }
}