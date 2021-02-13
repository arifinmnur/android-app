package com.kelsos.mbrc.platform.mediasession

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.Action
import com.kelsos.mbrc.R
import com.kelsos.mbrc.content.activestatus.PlayerState
import com.kelsos.mbrc.content.library.tracks.PlayingTrack
import com.kelsos.mbrc.di.modules.AppCoroutineDispatchers
import com.kelsos.mbrc.platform.mediasession.RemoteViewIntentBuilder.NEXT
import com.kelsos.mbrc.platform.mediasession.RemoteViewIntentBuilder.OPEN
import com.kelsos.mbrc.platform.mediasession.RemoteViewIntentBuilder.PLAY
import com.kelsos.mbrc.platform.mediasession.RemoteViewIntentBuilder.PREVIOUS
import com.kelsos.mbrc.platform.mediasession.RemoteViewIntentBuilder.getPendingIntent
import com.kelsos.mbrc.preferences.SettingsManager
import com.kelsos.mbrc.utilities.RemoteUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

class SessionNotificationManager(
  private val context: Application,
  private val sessionManager: RemoteSessionManager,
  private val settings: SettingsManager,
  private val appCoroutineDispatchers: AppCoroutineDispatchers,
  private val notificationManager: NotificationManager
) : INotificationManager {

  private val previous: String by lazy { context.getString(R.string.notification_action_previous) }
  private val play: String by lazy { context.getString(R.string.notification_action_play) }
  private val next: String by lazy { context.getString(R.string.notification_action_next) }

  private var notification: Notification? = null

  private var notificationData: NotificationData = NotificationData()

  init {
    createNotificationChannels()
  }

  fun update(notificationData: NotificationData) {
    notification = createBuilder(notificationData).build()
    notificationManager.notify(INotificationManager.NOW_PLAYING_PLACEHOLDER, notification)
  }

  private fun connectionChanged(connected: Boolean) {
    if (!connected) {
      cancel(NOW_PLAYING_PLACEHOLDER)
    } else {
      update(this.notificationData)
    }
  }

  private fun createNotificationChannels() {
    val channel = channel(context)
    if (channel === null) {
      return
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      notificationManager.createNotificationChannel(channel)
    }
  }

  private fun createBuilder(notificationData: NotificationData): NotificationCompat.Builder {
    val mediaStyle = androidx.media.app.NotificationCompat.MediaStyle()
    mediaStyle.setMediaSession(sessionManager.mediaSessionToken)

    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
    val resId = if (notificationData.playerState == PlayerState.PLAYING) {
      R.drawable.ic_action_pause
    } else {
      R.drawable.ic_action_play
    }

    builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
      .setSmallIcon(R.drawable.ic_mbrc_status)
      .setStyle(mediaStyle.setShowActionsInCompactView(1, 2))
      .addAction(getPreviousAction())
      .addAction(getPlayAction(resId))
      .addAction(getNextAction())

    builder.priority = NotificationCompat.PRIORITY_LOW
    builder.setOnlyAlertOnce(true)

    if (notificationData.cover != null) {
      builder.setLargeIcon(this.notificationData.cover)
    } else {
      val icon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_image_no_cover)
      builder.setLargeIcon(icon)
    }

    with(notificationData.track) {
      builder.setContentTitle(title)
        .setContentText(artist)
        .setSubText(album)
    }

    builder.setContentIntent(getPendingIntent(OPEN, context))

    return builder
  }

  private fun getPreviousAction(): Action {
    val previousIntent = getPendingIntent(PREVIOUS, context)
    return Action.Builder(R.drawable.ic_action_previous, previous, previousIntent).build()
  }

  private fun getPlayAction(playStateIcon: Int): Action {
    val playIntent = getPendingIntent(PLAY, context)

    return Action.Builder(playStateIcon, play, playIntent).build()
  }

  private fun getNextAction(): Action {
    val nextIntent = getPendingIntent(NEXT, context)
    return Action.Builder(R.drawable.ic_action_next, next, nextIntent).build()
  }

  override fun cancel(notificationId: Int) {
    notificationManager.cancel(notificationId)
  }

  override fun trackChanged(playingTrack: PlayingTrack) {
    GlobalScope.async {
      notificationData = with(playingTrack.coverUrl) {
        val cover = if (isNotEmpty()) {
          RemoteUtils.loadBitmap(this).await()
        } else {
          null
        }
        notificationData.copy(track = playingTrack, cover = cover)
      }

      update(notificationData)
    }
  }

  override fun connectionStateChanged(connected: Boolean) {
    if (connected) {
      cancel(NOW_PLAYING_PLACEHOLDER)
    } else {
      notification = createBuilder(this.notificationData).build()
    }
  }

  override fun playerStateChanged(state: String) {
    if (notificationData.playerState == state) {
      return
    }

    notificationData = notificationData.copy(playerState = state)
    update(notificationData)
  }

  companion object {
    const val NOW_PLAYING_PLACEHOLDER = 15613
    const val CHANNEL_ID = "mbrc_session_01"

    fun channel(context: Context): NotificationChannel? {
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        return null
      }

      val channelName = context.getString(R.string.notification__session_channel_name)
      val channelDescription = context.getString(R.string.notification__session_channel_description)

      val channel = NotificationChannel(
        CHANNEL_ID,
        channelName,
        NotificationManager.IMPORTANCE_DEFAULT
      )

      return channel.apply {
        this.description = channelDescription
        enableLights(false)
        enableVibration(false)
        setSound(null, null)
      }
    }
  }
}