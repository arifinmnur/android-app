package com.kelsos.mbrc.content.library.albums

import androidx.paging.DataSource
import com.kelsos.mbrc.di.modules.AppCoroutineDispatchers
import com.kelsos.mbrc.networking.ApiBase
import com.kelsos.mbrc.networking.protocol.Protocol
import com.kelsos.mbrc.utilities.epoch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.withContext

class AlbumRepositoryImpl(
  private val dao: AlbumDao,
  private val api: ApiBase,
  private val dispatchers: AppCoroutineDispatchers
) : AlbumRepository {
  private val mapper = AlbumDtoMapper()

  override suspend fun getAlbumsByArtist(artist: String): DataSource.Factory<Int, AlbumEntity> =
    dao.getAlbumsByArtist(artist)

  override suspend fun getAll(): DataSource.Factory<Int, AlbumEntity> = dao.getAll()

  override suspend fun getAndSaveRemote(): DataSource.Factory<Int, AlbumEntity> {
    getRemote()
    return dao.getAll()
  }

  override suspend fun getRemote() {
    val added = epoch()
    withContext(dispatchers.network) {
      api.getAllPages(Protocol.LibraryBrowseAlbums, AlbumDto::class)
        .onCompletion {
          dao.removePreviousEntries(added)
        }
        .collect {
          dao.insert(it.map { mapper.map(it) })
        }
    }
  }

  override suspend fun search(term: String): DataSource.Factory<Int, AlbumEntity> = dao.search(term)

  override suspend fun cacheIsEmpty(): Boolean = dao.count() == 0L

  override suspend fun count(): Long = dao.count()

  override suspend fun getAlbumsSorted(
    @Sorting.Fields order: Int,
    ascending: Boolean
  ): DataSource.Factory<Int, AlbumEntity> {
    return when (order) {
      Sorting.ALBUM -> {
        if (ascending) {
          dao.getSortedByAlbumAsc()
        } else {
          dao.getSortedByAlbumDesc()
        }
      }
      Sorting.ALBUM_ARTIST__ALBUM -> {
        if (ascending) {
          dao.getSortedByAlbumArtistAndAlbumAsc()
        } else {
          dao.getSortedByAlbumArtistAndAlbumDesc()
        }
      }
      Sorting.ALBUM_ARTIST__YEAR__ALBUM -> {
        if (ascending) {
          dao.getSortedByAlbumArtistAndYearAndAlbumAsc()
        } else {
          dao.getSortedByAlbumArtistAndYearAndAlbumDesc()
        }
      }
      Sorting.ARTIST__ALBUM -> {
        if (ascending) {
          dao.getSortedByArtistAndAlbumAsc()
        } else {
          dao.getSortedByArtistAndAlbumDesc()
        }
      }
      Sorting.GENRE__ALBUM_ARTIST__ALBUM -> {
        if (ascending) {
          dao.getSortedByGenreAndAlbumArtistAndAlbumAsc()
        } else {
          dao.getSortedByGenreAndAlbumArtistAndAlbumDesc()
        }
      }
      Sorting.YEAR__ALBUM -> {
        if (ascending) {
          dao.getSortedByYearAndAlbumAsc()
        } else {
          dao.getSortedByYearAndAlbumDesc()
        }
      }
      Sorting.YEAR__ALBUM_ARTIST__ALBUM -> {
        if (ascending) {
          dao.getSortedByYearAndAlbumArtistAndAlbumAsc()
        } else {
          dao.getSortedByYearAndAlbumArtistAndAlbumDesc()
        }
      }
      else -> throw IllegalArgumentException("Invalid option")
    }
  }
}