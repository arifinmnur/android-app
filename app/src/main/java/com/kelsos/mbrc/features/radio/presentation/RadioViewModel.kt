package com.kelsos.mbrc.features.radio.presentation

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.kelsos.mbrc.common.utilities.AppCoroutineDispatchers
import com.kelsos.mbrc.common.utilities.paged
import com.kelsos.mbrc.features.queue.QueueUseCase
import com.kelsos.mbrc.features.radio.domain.RadioStation
import com.kelsos.mbrc.features.radio.repository.RadioRepository
import com.kelsos.mbrc.ui.BaseViewModel
import kotlinx.coroutines.launch

class RadioViewModel(
  private val radioRepository: RadioRepository,
  private val queueUseCase: QueueUseCase,
  private val dispatchers: AppCoroutineDispatchers
) : BaseViewModel<RadioUiMessages>(dispatchers) {

  val radios: LiveData<PagedList<RadioStation>> = radioRepository.getAll().paged()

  fun reload() {
    scope.launch(dispatchers.network) {
      val result = radioRepository.getRemote()
        .fold({
          RadioUiMessages.RefreshFailed
        }, {
          RadioUiMessages.RefreshSuccess
        })
      emit(result)
    }
  }

  fun play(path: String) {
    scope.launch(dispatchers.network) {
      val response = queueUseCase.queuePath(path)
      val uiMessage = if (response.success) {
        RadioUiMessages.QueueSuccess
      } else {
        RadioUiMessages.QueueFailed
      }
      emit(uiMessage)
    }
  }
}