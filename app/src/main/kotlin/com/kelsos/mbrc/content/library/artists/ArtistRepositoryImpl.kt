package com.kelsos.mbrc.content.library.artists

import androidx.paging.DataSource
import com.kelsos.mbrc.di.modules.AppCoroutineDispatchers
import com.kelsos.mbrc.networking.ApiBase
import com.kelsos.mbrc.networking.protocol.Protocol
import com.kelsos.mbrc.utilities.epoch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.withContext

class ArtistRepositoryImpl(
  private val dao: ArtistDao,
  private val api: ApiBase,
  private val dispatchers: AppCoroutineDispatchers
) : ArtistRepository {

  private val mapper = ArtistDtoMapper()

  override suspend fun count(): Long = withContext(dispatchers.database) {
    dao.count()
  }

  override suspend fun getArtistByGenre(genre: String): DataSource.Factory<Int, ArtistEntity> =
    withContext(dispatchers.database) {
      dao.getArtistByGenre(genre)
    }

  override suspend fun getAll(): DataSource.Factory<Int, ArtistEntity> =
    withContext(dispatchers.database) {
      dao.getAll()
    }

  override suspend fun getRemote() {
    withContext(dispatchers.network) {
      val added = epoch()
      api.getAllPages(Protocol.LibraryBrowseArtists, ArtistDto::class)
        .onCompletion {
          dao.removePreviousEntries(added)
        }
        .collect {
          dao.insertAll(it.map { mapper.map(it).apply { dateAdded = added } })
        }
    }
  }

  override suspend fun search(term: String): DataSource.Factory<Int, ArtistEntity> =
    withContext(dispatchers.database) {
      dao.search(term)
    }

  override suspend fun getAlbumArtistsOnly(): DataSource.Factory<Int, ArtistEntity> =
    dao.getAlbumArtists()

  override suspend fun getAllRemoteAndShowAlbumArtist(): DataSource.Factory<Int, ArtistEntity> {
    getRemote()
    return withContext(dispatchers.database) {
      dao.getAlbumArtists()
    }
  }

  override suspend fun cacheIsEmpty(): Boolean = withContext(dispatchers.database) {
    dao.count() == 0L
  }
}