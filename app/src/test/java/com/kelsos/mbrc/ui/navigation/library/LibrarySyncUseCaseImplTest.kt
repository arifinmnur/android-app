package com.kelsos.mbrc.ui.navigation.library

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.kelsos.mbrc.TestApplication
import com.kelsos.mbrc.content.library.albums.AlbumRepository
import com.kelsos.mbrc.content.library.artists.ArtistRepository
import com.kelsos.mbrc.content.library.genres.GenreRepository
import com.kelsos.mbrc.content.library.tracks.TrackRepository
import com.kelsos.mbrc.content.playlists.PlaylistRepository
import com.kelsos.mbrc.content.sync.LibrarySyncUseCase
import com.kelsos.mbrc.content.sync.LibrarySyncUseCaseImpl
import com.kelsos.mbrc.metrics.SyncMetrics
import com.kelsos.mbrc.utils.testDispatcherModule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.dsl.module.module
import org.koin.experimental.builder.create
import org.koin.standalone.StandAloneContext.startKoin
import org.koin.standalone.StandAloneContext.stopKoin
import org.koin.standalone.inject
import org.koin.test.KoinTest
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class)
class LibrarySyncUseCaseImplTest : KoinTest {

  private val genreRepository: GenreRepository by inject()
  private val artistRepository: ArtistRepository by inject()
  private val albumRepository: AlbumRepository by inject()
  private val trackRepository: TrackRepository by inject()
  private val playlistRepository: PlaylistRepository by inject()
  private val librarySyncUseCase: LibrarySyncUseCase by inject()
  private val syncMetrics: SyncMetrics by inject()

  private val testModule = module {
    single<LibrarySyncUseCase> { create<LibrarySyncUseCaseImpl>() }
    single { mockk<GenreRepository>() }
    single { mockk<ArtistRepository>() }
    single { mockk<AlbumRepository>() }
    single { mockk<TrackRepository>() }
    single { mockk<PlaylistRepository>() }
    single { mockk<SyncMetrics>() }
  }

  @Before
  fun setUp() {
    startKoin(listOf(testModule, testDispatcherModule))
    every { syncMetrics.librarySyncComplete(any()) } answers { }
    every { syncMetrics.librarySyncStarted() } answers { }
    every { syncMetrics.librarySyncFailed() } answers { }
  }

  @After
  fun tearDown() {
    stopKoin()
  }

  @Test
  fun emptyLibraryAutoSync() {
    mockCacheState(true)
    mockSuccessfulRepositoryResponse()

    runBlocking {
      val result = librarySyncUseCase.sync(true)
      assertThat(result.isRight()).isTrue()
      assertThat(result.toOption().nonEmpty()).isTrue()
    }
  }

  @Test
  fun nonEmptyLibraryAutoSync() {

    mockCacheState(false)
    mockSuccessfulRepositoryResponse()

    runBlocking {
      val result = librarySyncUseCase.sync(true)
      assertThat(result.isRight()).isTrue()
      assertThat(result.toOption().nonEmpty()).isFalse()
    }

    assertThat(librarySyncUseCase.isRunning()).isFalse()
  }


  private fun mockCacheState(isEmpty: Boolean) {
    coEvery { genreRepository.cacheIsEmpty() } returns isEmpty
    coEvery { artistRepository.cacheIsEmpty() } returns isEmpty
    coEvery { albumRepository.cacheIsEmpty() } returns isEmpty
    coEvery { trackRepository.cacheIsEmpty() } returns isEmpty

    coEvery { genreRepository.count() } returns if (isEmpty) 0 else 1
    coEvery { artistRepository.count() } returns if (isEmpty) 0 else 1
    coEvery { albumRepository.count() } returns if (isEmpty) 0 else 1
    coEvery { trackRepository.count() } returns if (isEmpty) 0 else 1
  }

  private fun mockSuccessfulRepositoryResponse() {
    coEvery { genreRepository.getRemote() } coAnswers { delay(1) }
    coEvery { artistRepository.getRemote() } coAnswers { delay(1) }
    coEvery { albumRepository.getRemote() } coAnswers { delay(1) }
    coEvery { trackRepository.getRemote() } coAnswers { delay(1) }
    coEvery { playlistRepository.getRemote() } coAnswers { delay(1) }
  }
}