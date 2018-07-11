package com.kelsos.mbrc.ui.navigation.radio

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import com.kelsos.mbrc.content.radios.RadioRepository
import com.kelsos.mbrc.content.radios.RadioStationEntity
import com.kelsos.mbrc.di.modules.AppCoroutineDispatchers
import com.kelsos.mbrc.utilities.paged
import kotlinx.coroutines.runBlocking

class RadioViewModel(
  private val radioRepository: RadioRepository,
  private val dispatchers: AppCoroutineDispatchers
) : ViewModel() {

  val radios: LiveData<PagedList<RadioStationEntity>> = runBlocking(dispatchers.disk) {
    radioRepository.getAll()
  }.paged()

  fun refresh() {
    runBlocking {
      radioRepository.getRemote()
    }
  }

  fun play(path: String) {
  }
}