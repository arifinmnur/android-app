package com.kelsos.mbrc.interfaces.data

import com.raizlabs.android.dbflow.list.FlowCursorList

interface Repository<T : Data> {
  suspend fun getAllCursor(): FlowCursorList<T>
  suspend fun getAndSaveRemote(): FlowCursorList<T>
  suspend fun getRemote()
  suspend fun search(term: String): FlowCursorList<T>
  suspend fun cacheIsEmpty(): Boolean
  suspend fun count(): Long
}
