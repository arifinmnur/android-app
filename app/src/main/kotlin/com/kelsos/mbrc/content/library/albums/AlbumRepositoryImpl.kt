package com.kelsos.mbrc.content.library.albums

import androidx.lifecycle.LiveData
import com.kelsos.mbrc.di.modules.AppDispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AlbumRepositoryImpl
@Inject
constructor(
  private val dao: AlbumDao,
  private val remoteDataSource: RemoteAlbumDataSource,
  private val dispatchers: AppDispatchers
) : AlbumRepository {
  private val mapper = AlbumDtoMapper()

  override suspend fun getAlbumsByArtist(artist: String): LiveData<List<AlbumEntity>> =
    dao.getAlbumsByArtist(artist)

  override suspend fun getAll(): LiveData<List<AlbumEntity>> = dao.getAll()

  override suspend fun getAndSaveRemote(): LiveData<List<AlbumEntity>> {
    getRemote()
    return dao.getAll()
  }

  override suspend fun getRemote() {
    dao.deleteAll()
    withContext(dispatchers.io) {
      remoteDataSource.fetch().collect {
        dao.insert(it.map { mapper.map(it) })
      }
    }
  }

  override suspend fun search(term: String): LiveData<List<AlbumEntity>> = dao.search(term)

  override suspend fun cacheIsEmpty(): Boolean = dao.count() == 0L

  override suspend fun count(): Long = dao.count()

  override suspend fun getAlbumsSorted(
    @Sorting.Fields order: Int,
    ascending: Boolean
  ): LiveData<List<AlbumEntity>> {
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
