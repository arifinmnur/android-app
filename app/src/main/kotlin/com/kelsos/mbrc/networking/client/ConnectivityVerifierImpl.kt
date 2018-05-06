package com.kelsos.mbrc.networking.client

import com.kelsos.mbrc.DeserializationAdapter
import com.kelsos.mbrc.di.modules.AppCoroutineDispatchers
import com.kelsos.mbrc.networking.RequestManager
import com.kelsos.mbrc.networking.protocol.Protocol
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ConnectivityVerifierImpl
@Inject constructor(
  private val deserializationAdapter: DeserializationAdapter,
  private val requestManager: RequestManager,
  private val dispatchers: AppCoroutineDispatchers
) : ConnectivityVerifier {

  private fun getMessage(response: String) = deserializationAdapter.objectify(
    response,
    SocketMessage::class
  )

  override suspend fun verify(): Boolean = withContext(dispatchers.network) {
    try {
      val connection = requestManager.openConnection(false)
      val verifyMessage = SocketMessage.create(Protocol.VerifyConnection)
      val response = requestManager.request(connection, verifyMessage)
      connection.close()
      val message = getMessage(response)

      if (Protocol.VerifyConnection == message.context) {
        return@withContext true
      } else {
        throw NoValidPluginConnection()
      }
    } catch (e: Exception) {
      return@withContext false
    }
  }

  class NoValidPluginConnection : Exception()
}