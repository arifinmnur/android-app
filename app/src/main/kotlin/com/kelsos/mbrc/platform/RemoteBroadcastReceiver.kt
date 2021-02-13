package com.kelsos.mbrc.platform

import android.Manifest.permission.READ_PHONE_STATE
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.telephony.TelephonyManager
import com.kelsos.mbrc.events.UserAction
import com.kelsos.mbrc.networking.client.UserActionUseCase
import com.kelsos.mbrc.networking.protocol.Protocol
import com.kelsos.mbrc.networking.protocol.VolumeInteractor
import com.kelsos.mbrc.platform.mediasession.RemoteViewIntentBuilder
import com.kelsos.mbrc.preferences.SettingsManager
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import timber.log.Timber

class RemoteBroadcastReceiver : BroadcastReceiver(), KoinComponent {

  private val settingsManager: SettingsManager by inject()
  private val volumeInteractor: VolumeInteractor by inject()
  private val userActionUseCase: UserActionUseCase by inject()

  /**
   * Initialized and installs the IntentFilter listening for the SONG_CHANGED
   * intent fired by the ReplyHandler or the PHONE_STATE intent fired by the
   * Android operating system.
   */
  fun filter(context: Context): IntentFilter {
    val hasPermission =
      context.checkSelfPermission(READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
    val handleCallAction = settingsManager.getCallAction() != SettingsManager.NONE

    return IntentFilter().apply {
      if (hasPermission && handleCallAction) {
        addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
      }
      addAction(RemoteViewIntentBuilder.PLAY_PRESSED)
      addAction(RemoteViewIntentBuilder.NEXT_PRESSED)
      addAction(RemoteViewIntentBuilder.CLOSE_PRESSED)
      addAction(RemoteViewIntentBuilder.PREVIOUS_PRESSED)
      addAction(RemoteViewIntentBuilder.CANCELLED_NOTIFICATION)
    }
  }

  override fun onReceive(context: Context, intent: Intent) {
    Timber.v("Incoming %s", intent)
    when (intent.action) {
      TelephonyManager.ACTION_PHONE_STATE_CHANGED -> {
        Timber.v("Incoming")
        val bundle = intent.extras ?: return
        val state = bundle.getString(TelephonyManager.EXTRA_STATE)
        if (TelephonyManager.EXTRA_STATE_RINGING.equals(state!!, ignoreCase = true)) {
          handleRinging()
        }
      }
      RemoteViewIntentBuilder.PLAY_PRESSED -> {
        postAction(UserAction(Protocol.PlayerPlayPause, true))
      }
      RemoteViewIntentBuilder.NEXT_PRESSED -> {
        postAction(UserAction(Protocol.PlayerNext, true))
      }
      RemoteViewIntentBuilder.CLOSE_PRESSED -> {
        if (!RemoteService.SERVICE_STOPPING) {
          context.stopService(Intent(context, RemoteService::class.java))
        }
      }
      RemoteViewIntentBuilder.PREVIOUS_PRESSED -> {
        postAction(UserAction(Protocol.PlayerPrevious, true))
      }
      RemoteViewIntentBuilder.CANCELLED_NOTIFICATION -> {
        if (!RemoteService.SERVICE_STOPPING) {
          context.stopService(Intent(context, RemoteService::class.java))
        }
      }
    }
  }

  private fun handleRinging() {
    when (settingsManager.getCallAction()) {
      SettingsManager.PAUSE -> postAction(UserAction(Protocol.PlayerPause, true))
      SettingsManager.STOP -> postAction(UserAction(Protocol.PlayerStop, true))
      SettingsManager.REDUCE -> volumeInteractor.reduceVolume()
    }
  }

  private fun postAction(data: UserAction) {
    userActionUseCase.perform(data)
  }
}