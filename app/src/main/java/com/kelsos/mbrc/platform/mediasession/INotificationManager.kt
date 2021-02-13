package com.kelsos.mbrc.platform.mediasession

import com.kelsos.mbrc.content.activestatus.PlayerState
import com.kelsos.mbrc.features.library.PlayingTrack

interface INotificationManager {

  fun cancel(notificationId: Int = NOW_PLAYING_PLACEHOLDER)

  fun trackChanged(playingTrack: PlayingTrack)

  fun playerStateChanged(state: PlayerState)

  fun connectionStateChanged(connected: Boolean)

  companion object {
    const val NOW_PLAYING_PLACEHOLDER = 15613
    const val CHANNEL_ID = "mbrc_session_01"
  }
}
