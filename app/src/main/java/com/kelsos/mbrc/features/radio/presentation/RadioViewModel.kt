package com.kelsos.mbrc.features.radio.presentation

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.kelsos.mbrc.di.modules.AppCoroutineDispatchers
import com.kelsos.mbrc.features.radio.domain.RadioStation
import com.kelsos.mbrc.features.radio.repository.RadioRepository
import com.kelsos.mbrc.helper.QueueHandler
import com.kelsos.mbrc.ui.BaseViewModel
import com.kelsos.mbrc.utilities.paged
import kotlinx.coroutines.launch

class RadioViewModel(
  private val radioRepository: RadioRepository,
  private val queue: QueueHandler,
  private val dispatchers: AppCoroutineDispatchers
) : BaseViewModel<RadioRefreshResult>(dispatchers) {

  val radios: LiveData<PagedList<RadioStation>> = radioRepository.getAll().paged()

  fun reload() {
    scope.launch(dispatchers.network) {
      radioRepository.getRemote()
        .toEither()
        .fold({
          RadioRefreshResult.RefreshFailed
        }, {
          RadioRefreshResult.RefreshSuccess
        })
    }
  }

  fun play(path: String) {
    scope.launch(dispatchers.network) {
      queue.queuePath(path)
    }
  }
}