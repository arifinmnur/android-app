package com.kelsos.mbrc.interfaces.data

import androidx.lifecycle.LiveData

interface Repository<T : Data> {
  suspend fun getAll(): LiveData<List<T>>
  suspend fun getAndSaveRemote(): LiveData<List<T>>
  suspend fun getRemote()
  suspend fun search(term: String): LiveData<List<T>>
  suspend fun cacheIsEmpty(): Boolean
  suspend fun count(): Long
}
