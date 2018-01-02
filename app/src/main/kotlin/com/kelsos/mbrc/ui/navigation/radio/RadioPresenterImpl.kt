package com.kelsos.mbrc.ui.navigation.radio

import androidx.lifecycle.LiveData
import com.kelsos.mbrc.content.radios.RadioRepository
import com.kelsos.mbrc.content.radios.RadioStationEntity
import com.kelsos.mbrc.helper.QueueHandler
import com.kelsos.mbrc.mvp.BasePresenter
import kotlinx.coroutines.launch
import javax.inject.Inject

@RadioActivity.Presenter
class RadioPresenterImpl
@Inject
constructor(
  private val radioRepository: RadioRepository,
  private val queue: QueueHandler
) : BasePresenter<RadioView>(), RadioPresenter {

  private lateinit var radios: LiveData<List<RadioStationEntity>>

  override fun load() {
    view().showLoading()
    scope.launch {
      try {
        onRadiosLoaded(radioRepository.getAndSaveRemote())
      } catch (e: Exception) {
        view().error(e)
      }
      view().hideLoading()
    }
  }

  private fun onRadiosLoaded(radios: LiveData<List<RadioStationEntity>>) {
    this.radios = radios
    this.radios.observe(this@RadioPresenterImpl, {
      if (it != null) {
        view().update(it)
      }
    })
  }

  override fun refresh() {
    view().showLoading()
    scope.launch {
      try {
        onRadiosLoaded(radioRepository.getAndSaveRemote())
      } catch (e: Exception) {
        view().error(e)
      }
      view().hideLoading()
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
