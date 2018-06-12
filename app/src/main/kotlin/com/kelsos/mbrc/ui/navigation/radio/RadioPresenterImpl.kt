package com.kelsos.mbrc.ui.navigation.radio

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.paging.PagedList
import com.kelsos.mbrc.content.radios.RadioRepository
import com.kelsos.mbrc.content.radios.RadioStationEntity
import com.kelsos.mbrc.helper.QueueHandler
import com.kelsos.mbrc.mvp.BasePresenter
import com.kelsos.mbrc.utilities.paged
import kotlinx.coroutines.launch
import javax.inject.Inject

class RadioPresenterImpl
@Inject
constructor(
  private val radioRepository: RadioRepository,
  private val queue: QueueHandler
) : BasePresenter<RadioView>(), RadioPresenter {

  private lateinit var radios: LiveData<PagedList<RadioStationEntity>>

  override fun load() {
    view().loading(true)
    scope.launch {
      try {
        onRadiosLoaded(radioRepository.getAndSaveRemote())
      } catch (e: Exception) {
        view().error(e)
      }
      view().loading(false)
    }
  }

  private fun onRadiosLoaded(factory: DataSource.Factory<Int, RadioStationEntity>) {
    radios = factory.paged()
    radios.observe(this@RadioPresenterImpl, {
      if (it != null) {
        view().update(it)
      }
    })
  }

  override fun refresh() {
    view().loading(true)
    scope.launch {
      try {
        onRadiosLoaded(radioRepository.getAndSaveRemote())
      } catch (e: Exception) {
        view().error(e)
      }
      view().loading(false)
    }
  }

  override fun play(path: String) {
    scope.launch {
      try {
        queue.queuePath(path)
        view().radioPlaySuccessful()
      } catch (e: Exception) {
        view().radioPlayFailed(e)
      }
    }
  }
}