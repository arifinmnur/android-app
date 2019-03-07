package com.kelsos.mbrc.networking

import com.kelsos.mbrc.data.DeserializationAdapter
import com.kelsos.mbrc.interfaces.data.RemoteDataSource.Companion.LIMIT
import com.kelsos.mbrc.networking.client.GenericSocketMessage
import com.kelsos.mbrc.networking.client.SocketMessage
import com.kelsos.mbrc.networking.protocol.Page
import com.kelsos.mbrc.networking.protocol.PageRange
import com.squareup.moshi.Types
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import kotlin.reflect.KClass

class ApiBase(
  private val deserializationAdapter: DeserializationAdapter,
  private val apiRequestManager: RequestManager
) {

  suspend fun <T> getItem(
    request: String,
    kClazz: KClass<T>,
    payload: Any = ""
  ): T where T : Any {
    val type = Types.newParameterizedType(GenericSocketMessage::class.java, kClazz.java)
    val connection = apiRequestManager.openConnection()
    val response = apiRequestManager.request(connection, SocketMessage.create(request, payload))
    connection.close()
    return deserializationAdapter.objectify<GenericSocketMessage<T>>(response, type).data
  }

  suspend fun <T : Any> getAllPages(request: String, clazz: KClass<T>): Flow<List<T>> {
    val inner = Types.newParameterizedType(Page::class.java, clazz.java)
    val type = Types.newParameterizedType(GenericSocketMessage::class.java, inner)

    return flow {
      val start = now()
      val connection = apiRequestManager.openConnection()
      for (currentPage in 0..Int.MAX_VALUE) {
        val pageStart = now()
        val offset = currentPage * LIMIT
        val range = getPageRange(offset, LIMIT)
        Timber.v("fetching $request offset $offset [$LIMIT]")
        val message = SocketMessage.create(request, range ?: "")
        val response = apiRequestManager.request(connection, message)
        val socketMessage = deserializationAdapter.objectify<GenericSocketMessage<Page<T>>>(response, type)

        Timber.v("duration ${now() - pageStart} ms")
        val page = socketMessage.data
        emit(page.data)
        if (page.offset > page.total) {
          break
        }
      }
      connection.close()
      Timber.v("duration ${System.currentTimeMillis() - start} ms")
    }
  }

  private fun getPageRange(offset: Int, limit: Int): PageRange? {
    return takeIf { limit > 0 }?.run {
      PageRange().apply {
        this.offset = offset
        this.limit = limit
      }
    }
  }

  private fun now(): Long {
    return System.currentTimeMillis()
  }
}