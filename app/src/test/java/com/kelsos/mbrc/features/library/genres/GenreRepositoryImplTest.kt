package com.kelsos.mbrc.features.library.genres

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.kelsos.mbrc.data.Database
import com.kelsos.mbrc.features.library.data.GenreDao
import com.kelsos.mbrc.features.library.dto.GenreDto
import com.kelsos.mbrc.features.library.repositories.GenreRepository
import com.kelsos.mbrc.features.library.repositories.GenreRepositoryImpl
import com.kelsos.mbrc.networking.ApiBase
import com.kelsos.mbrc.networking.protocol.Protocol
import com.kelsos.mbrc.utilities.paged
import com.kelsos.mbrc.utils.observeOnce
import com.kelsos.mbrc.utils.testDispatcherModule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.experimental.builder.singleBy
import org.koin.test.KoinTest
import org.koin.test.inject

@RunWith(AndroidJUnit4::class)
class GenreRepositoryImplTest : KoinTest {

  private val repository: GenreRepository by inject()

  private lateinit var db: Database
  private lateinit var genreDao: GenreDao

  @get:Rule
  val rule = InstantTaskExecutorRule()

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, Database::class.java)
      .allowMainThreadQueries()
      .build()
    genreDao = db.genreDao()
    startKoin {
      modules(listOf(testModule, testDispatcherModule))
    }
  }

  @After
  fun tearDown() {
    db.close()
    stopKoin()
  }

  @Test
  fun getAndSaveRemote() {
    runBlocking {
      assertThat(repository.cacheIsEmpty()).isTrue()
      repository.getRemote()
      repository.getAll().paged().observeOnce { list ->
        assertThat(list).hasSize(1200)
        assertThat(list.first().genre).isEqualTo("Metal0")
      }
    }
  }

  val testModule = module {
    singleBy<GenreRepository, GenreRepositoryImpl>()

    val mockApi = mockk<ApiBase>()

    coEvery { mockApi.getAllPages(Protocol.LibraryBrowseGenres, GenreDto::class) } answers {
      flow {
        emit((0 until 1200).map { GenreDto("Metal$it") })
      }
    }
    single { mockApi }
    single { genreDao }
  }
}