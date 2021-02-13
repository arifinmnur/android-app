package com.kelsos.mbrc.features.playlists.repository

import androidx.paging.DataSource
import arrow.core.Try
import com.kelsos.mbrc.di.modules.AppCoroutineDispatchers
import com.kelsos.mbrc.features.playlists.PlaylistDto
import com.kelsos.mbrc.features.playlists.PlaylistDtoMapper
import com.kelsos.mbrc.features.playlists.PlaylistEntityMapper
import com.kelsos.mbrc.features.playlists.data.PlaylistDao
import com.kelsos.mbrc.features.playlists.domain.Playlist
import com.kelsos.mbrc.interfaces.data.Repository
import com.kelsos.mbrc.networking.ApiBase
import com.kelsos.mbrc.networking.protocol.Protocol
import com.kelsos.mbrc.utilities.epoch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.withContext

interface PlaylistRepository : Repository<Playlist>

class PlaylistRepositoryImpl(
  private val dao: PlaylistDao,
  private val api: ApiBase,
  private val dispatchers: AppCoroutineDispatchers
) : PlaylistRepository {
  override suspend fun count(): Long = withContext(dispatchers.database) { dao.count() }

  override fun getAll(): DataSource.Factory<Int, Playlist> =
    dao.getAll().map { PlaylistEntityMapper.map(it) }

  override suspend fun getRemote(): Try<Unit> = Try {
    withContext(dispatchers.network) {
      val added = epoch()
      api.getAllPages(Protocol.PlaylistList, PlaylistDto::class)
        .onCompletion {
          withContext(dispatchers.database) {
            dao.removePreviousEntries(added)
          }
        }.collect { items ->
          val playlists = items.map {
            PlaylistDtoMapper.map(it).apply {
              this.dateAdded = added
            }
          }
          withContext(dispatchers.database) {
            dao.insertAll(playlists)
          }
        }
    }
  }

  override fun search(term: String): DataSource.Factory<Int, Playlist> =
    dao.search(term).map { PlaylistEntityMapper.map(it) }

  override suspend fun cacheIsEmpty(): Boolean =
    withContext(dispatchers.database) { dao.count() == 0L }
}