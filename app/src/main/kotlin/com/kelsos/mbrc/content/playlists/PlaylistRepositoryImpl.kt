package com.kelsos.mbrc.content.playlists

import androidx.paging.DataSource
import com.kelsos.mbrc.di.modules.AppCoroutineDispatchers
import com.kelsos.mbrc.networking.ApiBase
import com.kelsos.mbrc.networking.protocol.Protocol
import com.kelsos.mbrc.utilities.epoch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.withContext

class PlaylistRepositoryImpl(
  private val dao: PlaylistDao,
  private val api: ApiBase,
  private val dispatchers: AppCoroutineDispatchers
) : PlaylistRepository {
  private val mapper = PlaylistDtoMapper()

  override suspend fun count(): Long = withContext(dispatchers.database) { dao.count() }

  override suspend fun getAll(): DataSource.Factory<Int, PlaylistEntity> =
    withContext(dispatchers.database) { dao.getAll() }

  override suspend fun getRemote() {
    withContext(dispatchers.network) {
      val added = epoch()
      api.getAllPages(Protocol.PlaylistList, PlaylistDto::class)
        .onCompletion {
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

  override suspend fun search(term: String): DataSource.Factory<Int, PlaylistEntity> =
    withContext(dispatchers.database) { dao.search(term) }

  override suspend fun cacheIsEmpty(): Boolean =
    withContext(dispatchers.database) { dao.count() == 0L }
}