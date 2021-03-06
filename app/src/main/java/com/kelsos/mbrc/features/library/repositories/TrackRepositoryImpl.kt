package com.kelsos.mbrc.features.library.repositories

import androidx.paging.DataSource
import arrow.core.Either
import com.kelsos.mbrc.common.data.Progress
import com.kelsos.mbrc.common.utilities.AppCoroutineDispatchers
import com.kelsos.mbrc.common.utilities.epoch
import com.kelsos.mbrc.features.library.data.Track
import com.kelsos.mbrc.features.library.data.TrackDao
import com.kelsos.mbrc.features.library.data.TrackEntityMapper
import com.kelsos.mbrc.features.library.dto.TrackDto
import com.kelsos.mbrc.features.library.dto.TrackDtoMapper
import com.kelsos.mbrc.networking.ApiBase
import com.kelsos.mbrc.networking.protocol.Protocol
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.withContext

class TrackRepositoryImpl(
  private val dao: TrackDao,
  private val api: ApiBase,
  private val dispatchers: AppCoroutineDispatchers
) : TrackRepository {

  private val dtoMapper = TrackDtoMapper()
  private val entityMapper = TrackEntityMapper()

  override suspend fun count(): Long = withContext(dispatchers.database) { dao.count() }

  override fun getAll(): DataSource.Factory<Int, Track> {
    return dao.getAll().map { entityMapper.map(it) }
  }

  override fun getAlbumTracks(
    album: String,
    artist: String
  ): DataSource.Factory<Int, Track> {
    return dao.getAlbumTracks(album, artist).map { entityMapper.map(it) }
  }

  override fun getNonAlbumTracks(artist: String): DataSource.Factory<Int, Track> =
    dao.getNonAlbumTracks(artist).map { entityMapper.map(it) }

  override suspend fun getRemote(progress: Progress): Either<Throwable, Unit> = Either.catch {
    return@catch withContext(dispatchers.network) {
      val added = epoch()
      val allPages = api.getAllPages(
        Protocol.LibraryBrowseTracks,
        TrackDto::class,
        progress
      )
      allPages.onCompletion {
        withContext(dispatchers.database) {
          dao.removePreviousEntries(added)
        }
      }.collect { items ->
        val tracks = items.map { dtoMapper.map(it).apply { dateAdded = added } }
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

  override fun search(term: String): DataSource.Factory<Int, Track> {
    return dao.search(term).map { entityMapper.map(it) }
  }

  override fun getGenreTrackPaths(genre: String): List<String> =
    dao.getGenreTrackPaths(genre)

  override fun getArtistTrackPaths(artist: String): List<String> =
    dao.getArtistTrackPaths(artist)

  override fun getAlbumTrackPaths(album: String, artist: String): List<String> =
    dao.getAlbumTrackPaths(album, artist)

  override fun getAllTrackPaths(): List<String> = dao.getAllTrackPaths()

  override suspend fun cacheIsEmpty(): Boolean =
    withContext(dispatchers.database) { dao.count() == 0L }

  override suspend fun getById(id: Long): Track? {
    return withContext(dispatchers.database) {
      val entity = dao.getById(id) ?: return@withContext null
      return@withContext entityMapper.map(entity)
    }
  }
}
