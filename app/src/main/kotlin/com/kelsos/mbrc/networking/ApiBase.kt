package com.kelsos.mbrc.networking

import com.fasterxml.jackson.databind.ObjectMapper
import com.kelsos.mbrc.interfaces.data.RemoteDataSource.Companion.LIMIT
import com.kelsos.mbrc.networking.client.GenericSocketMessage
import com.kelsos.mbrc.networking.client.SocketMessage
import com.kelsos.mbrc.networking.protocol.Page
import com.kelsos.mbrc.networking.protocol.PageRange
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject
import kotlin.reflect.KClass

class ApiBase
  @Inject
constructor(
  private val mapper: ObjectMapper,
  private val apiRequestManager: RequestManager
) {

  suspend fun <T> getItem(
    request: String,
    kClazz: KClass<T>,
    payload: Any = ""
  ): T where T : Any {
    val factory = mapper.typeFactory
    val type = factory.constructParametricType(GenericSocketMessage::class.java, kClazz.java)
    val connection = apiRequestManager.openConnection()
    val response = apiRequestManager.request(connection, SocketMessage.create(request, payload))
    connection.close()
    return mapper.readValue<GenericSocketMessage<T>>(response, type).data
  }

  suspend fun <T : Any> getAllPages(request: String, clazz: KClass<T>): Flow<List<T>> {
    val factory = mapper.typeFactory
    val inner = factory.constructParametricType(Page::class.java, clazz.java)
    val type = factory.constructParametricType(GenericSocketMessage::class.java, inner)

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
        val socketMessage = mapper.readValue<GenericSocketMessage<Page<T>>>(response, type)

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