package com.kelsos.mbrc.content.library.tracks

import androidx.paging.DataSource
import com.kelsos.mbrc.di.modules.AppCoroutineDispatchers
import com.kelsos.mbrc.networking.ApiBase
import com.kelsos.mbrc.networking.protocol.Protocol
import com.kelsos.mbrc.utilities.epoch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.withContext

class TrackRepositoryImpl(
  private val dao: TrackDao,
  private val api: ApiBase,
  private val dispatchers: AppCoroutineDispatchers
) : TrackRepository {

  private val mapper = TrackDtoMapper()

  override suspend fun count(): Long = withContext(dispatchers.database) { dao.count() }

  override fun getAll(): DataSource.Factory<Int, TrackEntity> = dao.getAll()

  override suspend fun getAlbumTracks(
    album: String,
    artist: String
  ): DataSource.Factory<Int, TrackEntity> = withContext(dispatchers.database) {
    dao.getAlbumTracks(album, artist)
  }

  override suspend fun getNonAlbumTracks(artist: String): DataSource.Factory<Int, TrackEntity> =
    withContext(dispatchers.database) {
      dao.getNonAlbumTracks(artist)
    }

  override suspend fun getRemote() {
    withContext(dispatchers.network) {
      val added = epoch()
      api.getAllPages(Protocol.LibraryBrowseTracks, TrackDto::class).onCompletion {
        dao.removePreviousEntries(added)
      }.collect { items ->
        val tracks = items.map { mapper.map(it).apply { dateAdded = added } }
        val sources = tracks.map { it.src }

        withContext(dispatchers.database) {

          val matches = sources.chunked(50)
            .flatMap { dao.findMatchingIds(it) }
            .map { it.src to it.id }
            .toMap()

          val toUpdate = tracks.filter { matches.containsKey(it.src) }
          val toInsert = tracks.minus(toUpdate)

          dao.update(toUpdate.map { it.id = matches.getValue(it.src); it })
          dao.insertAll(toInsert)
        }
      }
    }
  }

  override fun search(term: String): DataSource.Factory<Int, TrackEntity> = dao.search(term)

  override suspend fun getGenreTrackPaths(genre: String): List<String> =
    dao.getGenreTrackPaths(genre)

  override suspend fun getArtistTrackPaths(artist: String): List<String> =
    withContext(dispatchers.database) { dao.getArtistTrackPaths(artist) }

  override suspend fun getAlbumTrackPaths(album: String, artist: String): List<String> =
    withContext(dispatchers.database) { dao.getAlbumTrackPaths(album, artist) }

  override suspend fun getAllTrackPaths(): List<String> =
    withContext(dispatchers.database) { dao.getAllTrackPaths() }

  override suspend fun cacheIsEmpty(): Boolean =
    withContext(dispatchers.database) { dao.count() == 0L }
}