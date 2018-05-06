package com.kelsos.mbrc.content.library.artists

import androidx.paging.DataSource
import com.kelsos.mbrc.di.modules.AppCoroutineDispatchers
import com.kelsos.mbrc.networking.ApiBase
import com.kelsos.mbrc.networking.protocol.Protocol
import com.kelsos.mbrc.utilities.epoch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ArtistRepositoryImpl
@Inject
constructor(
  private val dao: ArtistDao,
  private val api: ApiBase,
  private val dispatchers: AppCoroutineDispatchers
) : ArtistRepository {

  private val mapper = ArtistDtoMapper()

  override suspend fun getArtistByGenre(genre: String): DataSource.Factory<Int, ArtistEntity> =
    dao.getArtistByGenre(genre)

  override suspend fun getAll(): DataSource.Factory<Int, ArtistEntity> = dao.getAll()

  override suspend fun getAndSaveRemote(): DataSource.Factory<Int, ArtistEntity> {
    getRemote()
    return dao.getAll()
  }

  override suspend fun getRemote() {
    withContext(dispatchers.network) {
      val added = epoch()
      api.getAllPages(Protocol.LibraryBrowseArtists, ArtistDto::class)
        .onCompletion {
          dao.removePreviousEntries(added)
        }
        .collect {
          dao.insertAll(it.map { mapper.map(it) })
        }
    }
  }

  override suspend fun search(term: String): DataSource.Factory<Int, ArtistEntity> =
    dao.search(term)

  override suspend fun getAlbumArtistsOnly(): DataSource.Factory<Int, ArtistEntity> =
    dao.getAlbumArtists()

  override suspend fun getAllRemoteAndShowAlbumArtist(): DataSource.Factory<Int, ArtistEntity> {
    getRemote()
    return dao.getAlbumArtists()
  }

  override suspend fun cacheIsEmpty(): Boolean = dao.count() == 0L

  override suspend fun count(): Long = dao.count()
}