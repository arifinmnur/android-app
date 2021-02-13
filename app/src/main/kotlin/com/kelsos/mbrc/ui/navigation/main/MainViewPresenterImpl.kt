package com.kelsos.mbrc.ui.navigation.main

import com.kelsos.mbrc.constants.Const
import com.kelsos.mbrc.constants.Protocol
import com.kelsos.mbrc.constants.ProtocolEventType
import com.kelsos.mbrc.content.active_status.MainDataModel
import com.kelsos.mbrc.events.ConnectionStatusChangeEvent
import com.kelsos.mbrc.events.CoverChangedEvent
import com.kelsos.mbrc.events.LfmRatingChanged
import com.kelsos.mbrc.events.MessageEvent
import com.kelsos.mbrc.events.PlayStateChange
import com.kelsos.mbrc.events.RepeatChange
import com.kelsos.mbrc.events.ScrobbleChange
import com.kelsos.mbrc.events.ShuffleChange
import com.kelsos.mbrc.events.TrackInfoChangeEvent
import com.kelsos.mbrc.events.UpdatePosition
import com.kelsos.mbrc.events.UserAction
import com.kelsos.mbrc.events.VolumeChange
import com.kelsos.mbrc.events.bus.RxBus
import com.kelsos.mbrc.mvp.BasePresenter
import com.kelsos.mbrc.networking.connections.ConnectionModel
import com.kelsos.mbrc.preferences.SettingsManager
import com.kelsos.mbrc.repository.ModelInitializer
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class MainViewPresenterImpl
@Inject constructor(
  private val bus: RxBus,
  private val model: MainDataModel,
  private val connectionModel: ConnectionModel,
  private val settingsManager: SettingsManager,
  private val modelInitializer: ModelInitializer
) : BasePresenter<MainView>(), MainViewPresenter {

  override fun stop(): Boolean {
    postAction(UserAction(Protocol.PlayerStop, true))
    return true
  }

  override fun mute() {
    postAction(UserAction(Protocol.PlayerMute, Const.TOGGLE))
  }

  override fun shuffle() {
    postAction(UserAction(Protocol.PlayerShuffle, Const.TOGGLE))
  }

  override fun repeat() {
    postAction(UserAction(Protocol.PlayerRepeat, Const.TOGGLE))
  }

  override fun changeVolume(value: Int) {
    postAction(UserAction.create(Protocol.PlayerVolume, value))
  }

  override fun seek(position: Int) {
    postAction(UserAction.create(Protocol.NowPlayingPosition, position))
  }

  private fun load() {
    scope.launch {
      try {
        modelInitializer.initialize()
        checkIfAttached()
        view?.updateCover(model.coverPath)
        view?.updateLfmStatus(model.lfmStatus)
        view?.updateScrobbleStatus(model.isScrobblingEnabled)
        view?.updateRepeat(model.repeat)
        view?.updateShuffleState(model.shuffle)
        view?.updateVolume(model.volume, model.isMute)
        view?.updatePlayState(model.playState)
        view?.updateTrackInfo(model.trackInfo)
        view?.updateConnection(connectionModel.connection)
        view?.updateProgress(UpdatePosition(model.position.toInt(), model.duration.toInt()))
      } catch (e: Exception) {
        Timber.e(e, "Failed to load")
      }
    }

    if (settingsManager.shouldShowChangeLog()) {
      view?.showChangeLog()
    }
  }

  override fun requestNowPlayingPosition() {
    val action = UserAction.create(Protocol.NowPlayingPosition)
    bus.post(MessageEvent.action(action))
  }

  override fun toggleScrobbling() {
    bus.post(MessageEvent.action(UserAction(Protocol.PlayerScrobble, Const.TOGGLE)))
  }

  override fun attach(view: MainView) {
    super.attach(view)
    load()
    this.bus.register(
      this,
      CoverChangedEvent::class.java,
      { this.view?.updateCover(it.path) },
      true
    )
    this.bus.register(
      this,
      ShuffleChange::class.java,
      { this.view?.updateShuffleState(it.shuffleState) },
      true
    )
    this.bus.register(this, RepeatChange::class.java, { this.view?.updateRepeat(it.mode) }, true)
    this.bus.register(
      this,
      VolumeChange::class.java,
      { this.view?.updateVolume(it.volume, it.isMute) },
      true
    )
    this.bus.register(
      this,
      PlayStateChange::class.java,
      { this.view?.updatePlayState(it.state) },
      true
    )
    this.bus.register(
      this,
      TrackInfoChangeEvent::class.java,
      { this.view?.updateTrackInfo(it.trackInfo) },
      true
    )
    this.bus.register(
      this,
      ConnectionStatusChangeEvent::class.java,
      { this.view?.updateConnection(it.status) },
      true
    )
    this.bus.register(this, UpdatePosition::class.java, { this.view?.updateProgress(it) }, true)
    this.bus.register(
      this,
      ScrobbleChange::class.java,
      { this.view?.updateScrobbleStatus(it.isActive) },
      true
    )
    this.bus.register(
      this,
      LfmRatingChanged::class.java,
      { this.view?.updateLfmStatus(it.status) },
      true
    )
    this.bus.register(this, MessageEvent::class.java, {
      if (it.type == ProtocolEventType.InformClientPluginOutOfDate) {
        this.view?.notifyPluginOutOfDate()
      }
    }, true)
  }

  override fun detach() {
    super.detach()
    this.bus.unregister(this)
  }

  override fun play() {
    postAction(UserAction(Protocol.PlayerPlayPause, true))
  }

  override fun previous() {
    postAction(UserAction(Protocol.PlayerPrevious, true))
  }

  override fun next() {
    postAction(UserAction(Protocol.PlayerNext, true))
  }

  override fun lfmLove(): Boolean {
    bus.post(MessageEvent.action(UserAction(Protocol.NowPlayingLfmRating, Const.TOGGLE)))
    return true
  }


  /**
   * Posts a user action wrapped in a MessageEvent. The bus will
   * pass the MessageEvent through the Socket to the plugin.

   * @param action Any kind of UserAction available in the [Protocol]
   */
  private fun postAction(action: UserAction) {
    bus.post(MessageEvent.action(action))
  }
}
