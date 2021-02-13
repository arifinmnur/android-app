package com.kelsos.mbrc.features.radio.presentation

import androidx.test.ext.junit.runners.AndroidJUnit4
import arrow.core.Try
import com.google.common.truth.Truth.assertThat
import com.kelsos.mbrc.events.Event
import com.kelsos.mbrc.features.radio.repository.RadioRepository
import com.kelsos.mbrc.helper.QueueHandler
import com.kelsos.mbrc.helper.QueueResult
import com.kelsos.mbrc.utils.MockFactory
import com.kelsos.mbrc.utils.TestDispatchers
import com.kelsos.mbrc.utils.observeOnce
import io.mockk.CapturingSlot
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.net.SocketTimeoutException

@RunWith(AndroidJUnit4::class)
class RadioViewModelTest {

  private lateinit var repository: RadioRepository
  private lateinit var queueHandler: QueueHandler
  private lateinit var radioViewModel: RadioViewModel
  private lateinit var observer: (Event<RadioUiMessages>) -> Unit
  private lateinit var slot: CapturingSlot<Event<RadioUiMessages>>

  @Before
  fun setUp() {
    repository = mockk()
    queueHandler = mockk()
    observer = mockk()
    slot = slot()
    every { observer(capture(slot)) } just Runs
    every { repository.getAll() } answers { MockFactory(emptyList()) }
    radioViewModel = RadioViewModel(repository, queueHandler, TestDispatchers.dispatchers)
  }

  @Test
  fun `should notify the observer that refresh failed`() {
    coEvery { repository.getRemote() } coAnswers { Try.raiseError(SocketTimeoutException()) }
    radioViewModel.emitter.observeOnce(observer)
    radioViewModel.reload()
    verify(exactly = 1) { observer(any()) }
    assertThat(slot.captured.peekContent()).isEqualTo(RadioUiMessages.RefreshFailed)
  }

  @Test
  fun `should notify the observer that refresh succeeded`() {
    coEvery { repository.getRemote() } coAnswers { Try.invoke { } }
    radioViewModel.emitter.observeOnce(observer)
    radioViewModel.reload()
    verify(exactly = 1) { observer(any()) }
    assertThat(slot.captured.peekContent()).isEqualTo(RadioUiMessages.RefreshSuccess)
  }

  @Test
  fun `should call queue and notify success`() {
    val playArguments = slot<String>()
    coEvery { queueHandler.queuePath(capture(playArguments)) } answers {
      QueueResult(true, 0)
    }

    radioViewModel.emitter.observeOnce(observer)
    radioViewModel.play("http://radio.station")
    assertThat(playArguments.captured).contains("http://radio.station")
    assertThat(slot.captured.peekContent()).isEqualTo(RadioUiMessages.QueueSuccess)
  }

  @Test
  fun `should notify on network error`() {
    coEvery { queueHandler.queuePath(any()) } throws SocketTimeoutException()
    radioViewModel.emitter.observeOnce(observer)
    radioViewModel.play("http://radio.station")
    assertThat(slot.captured.peekContent()).isEqualTo(RadioUiMessages.NetworkError)
  }

  @Test
  fun `should notify on queue failure`() {
    coEvery { queueHandler.queuePath(any()) } answers {
      QueueResult(false, 0)
    }
    radioViewModel.emitter.observeOnce(observer)
    radioViewModel.play("http://radio.station")
    assertThat(slot.captured.peekContent()).isEqualTo(RadioUiMessages.QueueFailed)
  }
}