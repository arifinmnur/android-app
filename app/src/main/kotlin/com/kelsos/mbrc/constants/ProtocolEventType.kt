package com.kelsos.mbrc.constants

object ProtocolEventType {
  const val InitiateProtocolRequest = "InitiateProtocolRequest"
  const val ReduceVolume = "ReduceVolume"
  const val HandshakeComplete = "HandshakeComplete"
  const val InformClientNotAllowed = "InformClientNotAllowed"
  const val InformClientPluginOutOfDate = "InformClientPluginOutOfDate"
  const val UserAction = "UserAction"
  const val PluginVersionCheck = "PluginVersionCheck"
  const val TerminateService = "TerminateService"
}
