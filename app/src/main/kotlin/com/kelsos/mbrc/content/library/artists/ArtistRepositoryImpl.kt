package com.kelsos.mbrc.content.library.artists

import androidx.lifecycle.LiveData
import com.kelsos.mbrc.di.modules.AppDispatchers
import com.kelsos.mbrc.utilities.epoch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ArtistRepositoryImpl
@Inject
constructor(
  private val dao: ArtistDao,
  private val remoteDataSource: RemoteArtistDataSource,
  private val dispatchers: AppDispatchers
) : ArtistRepository {

  private val mapper = ArtistDtoMapper()

  override suspend fun getArtistByGenre(genre: String): LiveData<List<ArtistEntity>> =
    dao.getArtistByGenre(genre)

  override suspend fun getAll(): LiveData<List<ArtistEntity>> = dao.getAll()

  override suspend fun getAndSaveRemote(): LiveData<List<ArtistEntity>> {
    getRemote()
    return dao.getAll()
  }

  override suspend fun getRemote() {
    withContext(dispatchers.io) {
      val added = epoch()
      remoteDataSource.fetch()
        .onCompletion {
          dao.removePreviousEntries(added)
        }
        .collect {
          dao.insertAll(it.map { mapper.map(it) })
        }
    }
  }

  override suspend fun search(term: String): LiveData<List<ArtistEntity>> =
    dao.search(term)

  override suspend fun getAlbumArtistsOnly(): LiveData<List<ArtistEntity>> =
    dao.getAlbumArtists()

  override suspend fun getAllRemoteAndShowAlbumArtist(): LiveData<List<ArtistEntity>> {
    getRemote()
    return dao.getAlbumArtists()
  }

  override suspend fun cacheIsEmpty(): Boolean = dao.count() == 0L

  override suspend fun count(): Long = dao.count()
}
