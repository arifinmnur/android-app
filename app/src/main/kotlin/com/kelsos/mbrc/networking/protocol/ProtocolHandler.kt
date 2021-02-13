package com.kelsos.mbrc.networking.protocol

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.kelsos.mbrc.R
import com.kelsos.mbrc.constants.Const
import com.kelsos.mbrc.constants.Protocol
import com.kelsos.mbrc.constants.ProtocolEventType
import com.kelsos.mbrc.content.active_status.MainDataModel
import com.kelsos.mbrc.events.MessageEvent
import com.kelsos.mbrc.events.NotifyUser
import com.kelsos.mbrc.events.bus.RxBus
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProtocolHandler
@Inject
constructor(
  private val bus: RxBus,
  private val mapper: ObjectMapper,
  private val model: MainDataModel
) {
  private var isHandshakeComplete: Boolean = false

  fun resetHandshake() {
    isHandshakeComplete = false
  }

  fun preProcessIncoming(incoming: String) {
    try {
      val replies = incoming.split("\r\n".toRegex())
        .dropLastWhile(String::isEmpty)
        .toTypedArray()

      replies.forEach {
        Timber.v("received:: $it")

        val node = mapper.readValue(it, JsonNode::class.java)
        val context = node.path("context")
          .textValue()
          .trim()
          .toLowerCase(Locale.getDefault())

        if (context == Protocol.ClientNotAllowed) {
          bus.post(MessageEvent(ProtocolEventType.InformClientNotAllowed))
          return
        } else if (context == Protocol.CommandUnavailable) {
          bus.post(NotifyUser(R.string.party_mode__command_unavailable))
          return
        }

        if (!isHandshakeComplete) {
          when (context) {
            Protocol.Player -> bus.post(MessageEvent(ProtocolEventType.InitiateProtocolRequest))
            Protocol.ProtocolTag -> handleProtocolMessage(node)
            else -> return
          }
        }

        bus.post(MessageEvent(context, node.path(Const.DATA)))
      }
    } catch (e: Exception) {
      Timber.v(e, "Failure while processing incoming data")
    }
  }

  private fun handleProtocolMessage(node: JsonNode) {
    model.pluginProtocol = getProtocolVersion(node)
    if (model.apiOutOfDate) {
      bus.post(MessageEvent(ProtocolEventType.InformClientPluginOutOfDate))
    }
    isHandshakeComplete = true
    bus.post(MessageEvent(ProtocolEventType.HandshakeComplete, true))
  }

  private fun getProtocolVersion(node: JsonNode): Int = try {
    Integer.parseInt(node.path(Const.DATA).asText())
  } catch (ignore: Exception) {
    2
  }
}
