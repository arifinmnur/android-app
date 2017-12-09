package com.kelsos.mbrc.content.library.albums

import com.kelsos.mbrc.di.modules.AppDispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AlbumRepositoryImpl
@Inject
constructor(
  private val localDataSource: LocalAlbumDataSource,
  private val remoteDataSource: RemoteAlbumDataSource,
  private val dispatchers: AppDispatchers
) : AlbumRepository {

  override suspend fun getAlbumsByArtist(artist: String): List<Album> =
    localDataSource.getAlbumsByArtist(artist)

  override suspend fun getAllCursor(): List<Album> = localDataSource.loadAllCursor()

  override suspend fun getAndSaveRemote(): List<Album> {
    getRemote()
    return localDataSource.loadAllCursor()
  }

  override suspend fun getRemote() {
    localDataSource.deleteAll()
    withContext(dispatchers.io) {
      remoteDataSource.fetch().collect {
        localDataSource.saveAll(it)
      }
    }
  }

  override suspend fun search(term: String): List<Album> = localDataSource.search(term)

  override suspend fun cacheIsEmpty(): Boolean = localDataSource.isEmpty()

  override suspend fun count(): Long = localDataSource.count()

  override suspend fun getAlbumsSorted(
    @Sorting.Fields order: Int,
    ascending: Boolean
  ): List<Album> {
    return localDataSource.getAlbumsSorted(order, ascending)
  }
}
