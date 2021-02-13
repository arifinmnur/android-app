package com.kelsos.mbrc.ui.navigation.nowplaying

import com.kelsos.mbrc.constants.Protocol
import com.kelsos.mbrc.data.NowPlayingMoveRequest
import com.kelsos.mbrc.data.UserAction
import com.kelsos.mbrc.events.MessageEvent
import com.kelsos.mbrc.events.TrackInfoChangeEvent
import com.kelsos.mbrc.events.bus.RxBus
import com.kelsos.mbrc.model.MainDataModel
import com.kelsos.mbrc.mvp.BasePresenter
import com.kelsos.mbrc.now_playing.NowPlayingRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class NowPlayingPresenterImpl
@Inject
constructor(
  private val repository: NowPlayingRepository,
  private val bus: RxBus,
  private val model: MainDataModel
) : BasePresenter<NowPlayingView>(),
  NowPlayingPresenter {

  override fun reload(scrollToTrack: Boolean) {
    view?.showLoading()
    scope.launch {
      try {
        view?.update(repository.getAndSaveRemote())
        view?.trackChanged(model.trackInfo, scrollToTrack)
      } catch (e: Exception) {
        view?.failure(e)
      }
      view?.hideLoading()
    }
  }

  override fun load() {
    view?.showLoading()
    scope.launch {
      try {
        view?.update(repository.getAllCursor())
        view?.trackChanged(model.trackInfo, true)
      } catch (e: Exception) {
        view?.failure(e)
      }
      view?.hideLoading()
    }
  }

  override fun search(query: String) {
    bus.post(
      MessageEvent.action(
        UserAction(
          Protocol.NowPlayingListSearch,
          query.trim { it <= ' ' })
      )
    )
  }

  override fun moveTrack(from: Int, to: Int) {
    val data = NowPlayingMoveRequest(from, to)
    bus.post(MessageEvent.action(UserAction(Protocol.NowPlayingListMove, data)))
  }

  override fun play(position: Int) {
    bus.post(MessageEvent.action(UserAction(Protocol.NowPlayingListPlay, position)))
  }

  override fun attach(view: NowPlayingView) {
    super.attach(view)
    bus.register(
      this,
      TrackInfoChangeEvent::class.java,
      { this.view?.trackChanged(it.trackInfo) },
      true
    )
  }

  override fun detach() {
    super.detach()
    bus.unregister(this)
  }

  override fun removeTrack(position: Int) {
    bus.post(MessageEvent.action(UserAction(Protocol.NowPlayingListRemove, position)))
  }
}
