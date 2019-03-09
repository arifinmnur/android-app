package com.kelsos.mbrc.networking.client

import androidx.preference.PreferenceManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.kelsos.mbrc.TestApplication
import com.kelsos.mbrc.data.DeserializationAdapter
import com.kelsos.mbrc.data.DeserializationAdapterImpl
import com.kelsos.mbrc.data.SerializationAdapter
import com.kelsos.mbrc.data.SerializationAdapterImpl
import com.kelsos.mbrc.networking.RequestManager
import com.kelsos.mbrc.networking.RequestManagerImpl
import com.kelsos.mbrc.networking.connections.ConnectionRepository
import com.kelsos.mbrc.networking.connections.ConnectionSettingsEntity
import com.kelsos.mbrc.networking.protocol.Protocol
import com.kelsos.mbrc.preferences.ClientInformationModel
import com.kelsos.mbrc.preferences.ClientInformationModelImpl
import com.kelsos.mbrc.preferences.ClientInformationStore
import com.kelsos.mbrc.preferences.ClientInformationStoreImpl
import com.squareup.moshi.Moshi
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.dsl.module.module
import org.koin.experimental.builder.create
import org.koin.standalone.StandAloneContext.startKoin
import org.koin.standalone.StandAloneContext.stopKoin
import org.koin.standalone.inject
import org.koin.test.KoinTest
import org.robolectric.annotation.Config
import timber.log.Timber
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.ServerSocket
import java.util.Random
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class)
class ConnectivityVerifierImplTest : KoinTest {

  private val port: Int = 46000

  private val verifier: ConnectivityVerifier by inject()
  private val connectionRepository: ConnectionRepository by inject()
  private val moshi: Moshi by inject()

  @Before
  fun setUp() {
    startKoin(listOf(testModule))
  }

  @After
  fun tearDown() {
    stopKoin()
  }

  private fun startMockServer(
    prematureDisconnect: Boolean = false,
    responseContext: String = Protocol.VerifyConnection,
    json: Boolean = true
  ): ServerSocket {
    val random = Random()
    val executor: ExecutorService = Executors.newSingleThreadExecutor()
    val server = ServerSocket(port + random.nextInt(1000))
    val mockSocket = Runnable {
      server.soTimeout = 3000
      val messageAdapter = moshi.adapter(SocketMessage::class.java)

      while (true) {
        Timber.v("Listening on ${server.inetAddress.hostAddress}:${server.localPort}")
        val connection = server.accept()
        val input = InputStreamReader(connection!!.inputStream)
        val inputReader = BufferedReader(input)
        val line = inputReader.readLine()
        Timber.v("Received a message $line")

        val value = messageAdapter.fromJson(line)

        if (value?.context != Protocol.VerifyConnection) {
          connection.close()
          server.close()
          return@Runnable
        }

        if (prematureDisconnect) {
          connection.close()
          server.close()
          return@Runnable
        }

        val out = OutputStreamWriter(connection.outputStream, "UTF-8")
        val output = PrintWriter(BufferedWriter(out), true)
        val newLine = "\r\n"
        if (json) {
          val response = SocketMessage(context = responseContext)
          output.write(messageAdapter.toJson(response) + newLine + newLine)
        } else {
          output.write(responseContext + newLine + newLine)
        }
        output.flush()
        input.close()
        inputReader.close()
        out.close()
        output.close()
        connection.close()
        server.close()
        return@Runnable
      }
    }

    executor.execute(mockSocket)
    return server
  }

  @Test
  fun testSuccessfulVerification() {
    val verifier = this.verifier
    val server = startMockServer()

    coEvery { connectionRepository.getDefault() } answers {
      val settings = ConnectionSettingsEntity()
      settings.address = server.inetAddress.hostAddress
      settings.port = server.localPort
      return@answers settings
    }

    runBlocking { assertThat(verifier.verify()).isTrue() }
  }

  @Test
  fun testPrematureDisconnectDuringVerification() {
    val verifier = this.verifier
    val server = startMockServer(true)
    coEvery { connectionRepository.getDefault() } answers {
      val settings = ConnectionSettingsEntity()
      settings.address = server.inetAddress.hostAddress
      settings.port = server.localPort
      return@answers settings
    }

    runBlocking {
      try {
        verifier.verify()
      } catch (e: Exception) {
        assertThat(e).isInstanceOf(RuntimeException::class.java)
      }
    }
  }

  @Test
  fun testInvalidPluginResponseVerification() {
    val verifier = this.verifier
    val server = startMockServer(false, Protocol.ClientNotAllowed)
    coEvery { connectionRepository.getDefault() } answers {
      val settings = ConnectionSettingsEntity()
      settings.address = server.inetAddress.hostAddress
      settings.port = server.localPort
      return@answers settings
    }

    runBlocking {
      try {
        verifier.verify()
      } catch (e: Exception) {
        assertThat(e).isInstanceOf(ConnectivityVerifierImpl.NoValidPluginConnection::class.java)
      }
    }
  }

  @Test
  fun testVerificationNoConnection() {
    val verifier = this.verifier
    startMockServer(true)

    coEvery { connectionRepository.getDefault() } answers {
      return@answers null
    }

    runBlocking {
      try {
        verifier.verify()
      } catch (e: Exception) {
        assertThat(e).isInstanceOf(RuntimeException::class.java)
      }
    }
  }

  @Test
  fun testVerificationNoJsonPayload() {
    val verifier = this.verifier
    startMockServer(false, "payload", false)

    coEvery { connectionRepository.getDefault() } answers {
      return@answers null
    }

    runBlocking {
      try {
        verifier.verify()
      } catch (e: Exception) {
        assertThat(e).isInstanceOf(RuntimeException::class.java)
      }
    }
  }

  private val testModule = module {
    val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    single { Moshi.Builder().build() }
    single { mockk<ConnectionRepository>() }
    single<ClientInformationStore> { create<ClientInformationStoreImpl>() }
    single<ClientInformationModel> {
      ClientInformationModelImpl(
        PreferenceManager.getDefaultSharedPreferences(
          appContext
        )
      )
    }
    single<ConnectivityVerifier> { create<ConnectivityVerifierImpl>() }
    single<SerializationAdapter> { create<SerializationAdapterImpl>() }
    single<DeserializationAdapter> { create<DeserializationAdapterImpl>() }
    single<RequestManager> { create<RequestManagerImpl>() }
  }
}