package com.kelsos.mbrc.networking.client

import arrow.core.Either
import com.kelsos.mbrc.common.utilities.AppCoroutineDispatchers
import com.kelsos.mbrc.data.DeserializationAdapter
import com.kelsos.mbrc.networking.RequestManager
import com.kelsos.mbrc.networking.protocol.Protocol
import kotlinx.coroutines.withContext

class ConnectivityVerifierImpl(
  private val deserializationAdapter: DeserializationAdapter,
  private val requestManager: RequestManager,
  private val dispatchers: AppCoroutineDispatchers
) : ConnectivityVerifier {

  override suspend fun verify(): Either<Throwable, Boolean> = Either.catch {
    withContext(dispatchers.network) {
      val connection = requestManager.openConnection(false)
      val verifyMessage = SocketMessage.create(Protocol.VerifyConnection)
      val response = requestManager.request(connection, verifyMessage)
      connection.close()
      val (context, data) = deserializationAdapter.objectify(
        response,
        SocketMessage::class
      )

      if (Protocol.VerifyConnection != context) {
        throw NoValidPluginConnection()
      }
      return@withContext true
    }
  }

  class NoValidPluginConnection : Exception()
}