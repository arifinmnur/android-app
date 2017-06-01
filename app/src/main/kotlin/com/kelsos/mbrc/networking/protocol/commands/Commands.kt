package com.kelsos.mbrc.networking.protocol.commands

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.kelsos.mbrc.constants.Const
import com.kelsos.mbrc.constants.Protocol
import com.kelsos.mbrc.constants.ProtocolEventType
import com.kelsos.mbrc.content.active_status.MainDataModel
import com.kelsos.mbrc.events.MessageEvent
import com.kelsos.mbrc.events.UserAction
import com.kelsos.mbrc.events.bus.RxBus
import com.kelsos.mbrc.interfaces.ICommand
import com.kelsos.mbrc.interfaces.IEvent
import com.kelsos.mbrc.networking.MulticastConfigurationDiscovery
import com.kelsos.mbrc.networking.SocketAction
import com.kelsos.mbrc.networking.SocketActivityChecker
import com.kelsos.mbrc.networking.SocketClient
import com.kelsos.mbrc.networking.SocketMessage
import com.kelsos.mbrc.networking.connections.ConnectionStatusModel
import com.kelsos.mbrc.networking.protocol.ProtocolPayload
import com.kelsos.mbrc.platform.RemoteService
import com.kelsos.mbrc.preferences.SettingsManager
import com.kelsos.mbrc.utilities.RemoteUtils.getVersion
import timber.log.Timber
import java.io.IOException
import java.net.URL
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

class KeyVolumeDownCommand
@Inject constructor(
    private val model: MainDataModel,
    private val bus: RxBus
) : ICommand {

  override fun execute(e: IEvent) {
    if (model.volume >= 10) {
      val mod = model.volume % 10
      val volume: Int

      if (mod == 0) {
        volume = model.volume - 10
      } else if (mod < 5) {
        volume = model.volume - (10 + mod)
      } else {
        volume = model.volume - mod
      }

      bus.post(MessageEvent.action(UserAction(Protocol.PlayerVolume, volume)))
    }
  }
}

class KeyVolumeUpCommand
@Inject constructor(private val model: MainDataModel, private val bus: RxBus) : ICommand {

  override fun execute(e: IEvent) {
    if (model.volume <= 90) {
      val mod = model.volume % 10
      val volume: Int

      if (mod == 0) {
        volume = model.volume + 10
      } else if (mod < 5) {
        volume = model.volume + (10 - mod)
      } else {
        volume = model.volume + (20 - mod)
      }

      bus.post(MessageEvent.action(UserAction(Protocol.PlayerVolume, volume)))
    }
  }
}

class ProcessUserAction
@Inject constructor(private val socket: SocketClient) : ICommand {

  override fun execute(e: IEvent) {
    val action = e.data as UserAction
    socket.sendData(SocketMessage.create(action.context, action.data))
  }
}

class ProtocolPingHandle
@Inject constructor(
    private val client: SocketClient,
    private var activityChecker: SocketActivityChecker
) : ICommand {

  override fun execute(e: IEvent) {
    activityChecker.ping()
    client.sendData(SocketMessage.create(Protocol.PONG))
  }
}

class ProtocolPongHandle
@Inject constructor() : ICommand {
  override fun execute(e: IEvent) {
    Timber.d(e.data.toString())
  }
}

class ProtocolRequest
@Inject constructor(
    private val socket: SocketClient,
    private val settingsManager: SettingsManager
) : ICommand {

  override fun execute(e: IEvent) {
    val payload = ProtocolPayload(settingsManager.getClientId())
    payload.noBroadcast = false
    payload.protocolVersion = Protocol.ProtocolVersionNumber
    socket.sendData(SocketMessage.create(Protocol.ProtocolTag, payload))
  }
}

class ReduceVolumeOnRingCommand
@Inject constructor(
    private val model: MainDataModel,
    private val client: SocketClient
) : ICommand {

  override fun execute(e: IEvent) {
    if (model.isMute || model.volume == 0) {
      return
    }
    client.sendData(SocketMessage.create(Protocol.PlayerVolume, (model.volume * 0.2).toInt()))
  }
}

class StartDiscoveryCommand
@Inject constructor(private val mDiscovery: MulticastConfigurationDiscovery) : ICommand {

  override fun execute(e: IEvent) {
    mDiscovery.startDiscovery()
  }
}

class TerminateConnectionCommand
@Inject constructor(
    private val client: SocketClient,
    private val statusModel: ConnectionStatusModel
) : ICommand {

  override fun execute(e: IEvent) {
    statusModel.disconnected()
    client.socketManager(SocketAction.TERMINATE)
  }
}

class VersionCheckCommand
@Inject
internal constructor(
  private val model: MainDataModel,
  private val mapper: ObjectMapper,
  private val context: Application,
  private val manager: SettingsManager,
  private val bus: RxBus
) : ICommand {

  override fun execute(e: IEvent) {

    if (!manager.isPluginUpdateCheckEnabled()) {
      return
    }

    val calendar = Calendar.getInstance()
    calendar.timeInMillis = manager.getLastUpdated().time
    calendar.add(Calendar.DATE, 2)
    val nextCheck = Date(calendar.timeInMillis)
    val now = Date()

    if (nextCheck.after(now)) {
      Timber.d("waiting for next check: %s", nextCheck.time.toString())
      return
    }

    val jsonNode: JsonNode
    try {
      jsonNode = mapper.readValue(URL(CHECK_URL), JsonNode::class.java)
    } catch (e1: IOException) {
      Timber.d(e1, "While reading json node")
      return
    }

    var version: String? = null
    try {
      version = context.getVersion()
    } catch (e1: PackageManager.NameNotFoundException) {
      Timber.d(e1, "While reading the current version")
    }

    val vNode = jsonNode.path(Const.VERSIONS).path(version)

    val suggestedVersion = vNode.path(Const.PLUGIN).asText()

    if (suggestedVersion != model.pluginVersion) {
      var isOutOfDate = false

      val currentVersion =
        model.pluginVersion.split("\\.".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
      val latestVersion =
        suggestedVersion.split("\\.".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()

      var i = 0
      while (i < currentVersion.size && i < latestVersion.size && currentVersion[i] == latestVersion[i]) {
        i++
      }

      if (i < currentVersion.size && i < latestVersion.size) {
        val diff = Integer.valueOf(currentVersion[i]).compareTo(Integer.valueOf(latestVersion[i]))
        isOutOfDate = diff < 0
      }

      if (isOutOfDate) {
        bus.post(MessageEvent(ProtocolEventType.InformClientPluginOutOfDate))
      }
    }

    manager.setLastUpdated(now)
    Timber.d("last check on: %s", now.time.toString())
    Timber.d("plugin reported version: %s", model.pluginVersion)
    Timber.d("plugin suggested version: %s", suggestedVersion)
  }

  companion object {
    private const val CHECK_URL = "http://kelsos.net/musicbeeremote/versions.json"
  }
}

class TerminateServiceCommand
@Inject constructor(
  private val application: Application
) : ICommand {

  override fun execute(e: IEvent) {
    if (RemoteService.SERVICE_STOPPING) {
      return
    }
    application.run {
      stopService(Intent(this, RemoteService::class.java))
    }
  }
}
