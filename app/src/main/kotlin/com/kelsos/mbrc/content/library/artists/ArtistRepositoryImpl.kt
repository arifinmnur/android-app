package com.kelsos.mbrc.content.library.artists

import com.kelsos.mbrc.di.modules.AppDispatchers
import com.raizlabs.android.dbflow.list.FlowCursorList
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ArtistRepositoryImpl
@Inject
constructor(
  private val localDataSource: LocalArtistDataSource,
  private val remoteDataSource: RemoteArtistDataSource,
  private val dispatchers: AppDispatchers
) : ArtistRepository {

  override suspend fun getArtistByGenre(genre: String): FlowCursorList<Artist> =
    localDataSource.getArtistByGenre(genre)

  override suspend fun getAllCursor(): FlowCursorList<Artist> = localDataSource.loadAllCursor()

  override suspend fun getAndSaveRemote(): FlowCursorList<Artist> {
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

  override suspend fun search(term: String): FlowCursorList<Artist> = localDataSource.search(term)

  override suspend fun getAlbumArtistsOnly(): FlowCursorList<Artist> =
    localDataSource.getAlbumArtists()

  override suspend fun getAllRemoteAndShowAlbumArtist(): FlowCursorList<Artist> {
    getRemote()
    return localDataSource.getAlbumArtists()
  }

  override suspend fun cacheIsEmpty(): Boolean = localDataSource.isEmpty()

  override suspend fun count(): Long = localDataSource.count()
}
