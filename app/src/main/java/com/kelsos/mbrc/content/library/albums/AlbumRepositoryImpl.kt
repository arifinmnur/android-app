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
  private val view2model = AlbumViewMapper()

  override suspend fun count(): Long = withContext(dispatchers.database) { dao.count() }

  override fun getAlbumsByArtist(artist: String): DataSource.Factory<Int, Album> =
    dao.getAlbumsByArtist(artist).map { view2model.map(it) }

  override fun getAll(): DataSource.Factory<Int, Album> =
    dao.getAll().map { view2model.map(it) }

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

  override fun search(term: String): DataSource.Factory<Int, Album> =
    dao.search(term).map { view2model.map(it) }

  override suspend fun cacheIsEmpty(): Boolean =
    withContext(dispatchers.database) { dao.count() == 0L }
}