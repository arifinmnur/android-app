package com.kelsos.mbrc.content.sync

import arrow.core.Try
import com.kelsos.mbrc.content.library.albums.AlbumRepository
import com.kelsos.mbrc.content.library.artists.ArtistRepository
import com.kelsos.mbrc.content.library.genres.GenreRepository
import com.kelsos.mbrc.content.library.tracks.TrackRepository
import com.kelsos.mbrc.content.playlists.PlaylistRepository
import com.kelsos.mbrc.content.sync.LibrarySyncUseCase.*
import com.kelsos.mbrc.di.modules.AppCoroutineDispatchers
import com.kelsos.mbrc.metrics.SyncMetrics
import com.kelsos.mbrc.metrics.SyncedData
import kotlinx.coroutines.withContext
import timber.log.Timber

class LibrarySyncUseCaseImpl(
  private val genreRepository: GenreRepository,
  private val artistRepository: ArtistRepository,
  private val albumRepository: AlbumRepository,
  private val trackRepository: TrackRepository,
  private val playlistRepository: PlaylistRepository,
  private val metrics: SyncMetrics,
  private val dispatchers: AppCoroutineDispatchers,
) : LibrarySyncUseCase {

  private var running: Boolean = false

  override suspend fun sync(auto: Boolean): Int {

    if (isRunning()) {
      Timber.v("Sync is already running")
      return SyncResult.NO_OP
    }

    running = true
    Timber.v("Starting library metadata sync")

    metrics.librarySyncStarted()

    val result: Int = if (checkIfShouldSync(auto)) {
      Try {
        genreRepository.getRemote()
        artistRepository.getRemote()
        albumRepository.getRemote()
        trackRepository.getRemote()
        playlistRepository.getRemote()


        metrics.librarySyncComplete(syncStats())

        return@Try true
      }.toEither().fold({ SyncResult.FAILED }, { SyncResult.SUCCESS })
    } else {
      SyncResult.NO_OP
    }

    if (result == SyncResult.FAILED) {
      metrics.librarySyncFailed()
    } else {
      withContext(dispatchers.disk) {
        metrics.librarySyncComplete(syncStats())
      }
    }

    running = false
    return result
  }


  override suspend fun syncStats(): SyncedData {
    return SyncedData(
      genres = genreRepository.count(),
      artists = artistRepository.count(),
      albums = albumRepository.count(),
      tracks = trackRepository.count(),
      playlists = playlistRepository.count()
    )
  }

  private suspend fun checkIfShouldSync(auto: Boolean): Boolean = if (auto)
    isEmpty()
  else
    true


  private suspend fun isEmpty(): Boolean {
    return genreRepository.cacheIsEmpty() &&
      artistRepository.cacheIsEmpty() &&
      albumRepository.cacheIsEmpty() &&
      trackRepository.cacheIsEmpty()
  }

  override fun isRunning(): Boolean = running
}