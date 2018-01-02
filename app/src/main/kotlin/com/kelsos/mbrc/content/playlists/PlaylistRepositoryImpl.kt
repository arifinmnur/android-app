package com.kelsos.mbrc.content.playlists

import androidx.lifecycle.LiveData
import com.kelsos.mbrc.di.modules.AppDispatchers
import com.kelsos.mbrc.utilities.epoch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PlaylistRepositoryImpl
@Inject constructor(
  private val dao: PlaylistDao,
  private val remoteDataSource: RemotePlaylistDataSource,
  private val dispatchers: AppDispatchers
) : PlaylistRepository {
  private val mapper = PlaylistDtoMapper()

  override suspend fun getAll(): LiveData<List<PlaylistEntity>> = dao.getAll()

  override suspend fun getAndSaveRemote(): LiveData<List<PlaylistEntity>> {
    getRemote()
    return dao.getAll()
  }

  override suspend fun getRemote() {
    withContext(dispatchers.io) {
      val added = epoch()
      remoteDataSource.fetch().onCompletion {
        dao.removePreviousEntries(added)
      }.collect { items ->
        val playlists = items.map {
          mapper.map(it).apply {
            this.dateAdded = added
          }
        }
        dao.insertAll(playlists)
      }
    }
  }

  override suspend fun search(term: String): LiveData<List<PlaylistEntity>> =
    dao.search(term)

  override suspend fun cacheIsEmpty(): Boolean = dao.count() == 0L

  override suspend fun count(): Long = dao.count()
}
