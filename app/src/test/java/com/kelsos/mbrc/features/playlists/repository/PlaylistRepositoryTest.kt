package com.kelsos.mbrc.features.playlists.repository

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.kelsos.mbrc.TestApplication
import com.kelsos.mbrc.data.Database
import com.kelsos.mbrc.features.playlists.PlaylistDto
import com.kelsos.mbrc.features.playlists.data.PlaylistDao
import com.kelsos.mbrc.features.playlists.domain.Playlist
import com.kelsos.mbrc.networking.ApiBase
import com.kelsos.mbrc.networking.protocol.Protocol
import com.kelsos.mbrc.utilities.paged
import com.kelsos.mbrc.utils.TestData
import com.kelsos.mbrc.utils.TestData.mockApi
import com.kelsos.mbrc.utils.TestDataFactories
import com.kelsos.mbrc.utils.observeOnce
import com.kelsos.mbrc.utils.testDispatcherModule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.rx2.asFlow
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.dsl.module.module
import org.koin.experimental.builder.create
import org.koin.standalone.StandAloneContext.startKoin
import org.koin.standalone.StandAloneContext.stopKoin
import org.koin.standalone.inject
import org.koin.test.KoinTest
import org.robolectric.annotation.Config
import java.net.SocketTimeoutException

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class)
class PlaylistRepositoryTest : KoinTest {

  private lateinit var apiBase: ApiBase
  private lateinit var database: Database
  private lateinit var dao: PlaylistDao

  private val repository: PlaylistRepository by inject()

  @get:Rule
  val rule = InstantTaskExecutorRule()

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    database = TestData.createDB(context)
    dao = database.playlistDao()
    apiBase = mockk()

    startKoin(listOf(module {
      single { dao }
      single<PlaylistRepository> { create<PlaylistRepositoryImpl>() }
      single { apiBase }
    }, testDispatcherModule))
  }

  @After
  fun tearDown() {
    database.close()
    stopKoin()
  }

  @Test
  fun `sync is failure if there is an exception`() = runBlockingTest {
    coEvery {
      apiBase.getAllPages(
        Protocol.PlaylistList,
        PlaylistDto::class
      )
    } throws SocketTimeoutException()
    assertThat(repository.getRemote().isFailure()).isTrue()
  }

  @Test
  fun `it should be initially empty`() = runBlockingTest {
    assertThat(repository.cacheIsEmpty())
    assertThat(repository.count()).isEqualTo(0)
    repository.getAll().paged().observeOnce { list ->
      assertThat(list).isEmpty()
    }
  }

  @Test
  fun `sync remote playlists and update database`() = runBlockingTest {
    coEvery { apiBase.getAllPages(Protocol.PlaylistList, PlaylistDto::class) } answers {
      mockApi(20) {
        TestDataFactories.playlist(it)
      }.asFlow()
    }
    assertThat(repository.getRemote().isSuccess()).isTrue()
    assertThat(repository.count()).isEqualTo(20)
    repository.getAll().paged().observeOnce { result ->
      assertThat(result).hasSize(20)
    }
  }

  @Test
  fun `it should filter the playlists when searching`() = runBlockingTest {
    val extra = listOf(PlaylistDto(name = "Heavy Metal", url = """C:\library\metal.m3u"""))
    coEvery { apiBase.getAllPages(Protocol.PlaylistList, PlaylistDto::class) } answers {
      mockApi(5, extra) {
        TestDataFactories.playlist(it)
      }.asFlow()
    }

    assertThat(repository.getRemote().isSuccess()).isTrue()
    repository.search("Metal").paged().observeOnce {
      assertThat(it).hasSize(1)
      assertThat(it).containsExactly(
        Playlist(
          name = "Heavy Metal",
          url = """C:\library\metal.m3u""",
          id = 6
        )
      )
    }
  }
}