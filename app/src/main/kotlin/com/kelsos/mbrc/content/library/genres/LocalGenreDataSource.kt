package com.kelsos.mbrc.content.library.genres

import com.kelsos.mbrc.RemoteDatabase
import com.kelsos.mbrc.di.modules.AppDispatchers
import com.kelsos.mbrc.extensions.escapeLike
import com.kelsos.mbrc.interfaces.data.LocalDataSource
import com.raizlabs.android.dbflow.kotlinextensions.database
import com.raizlabs.android.dbflow.kotlinextensions.delete
import com.raizlabs.android.dbflow.kotlinextensions.from
import com.raizlabs.android.dbflow.kotlinextensions.modelAdapter
import com.raizlabs.android.dbflow.kotlinextensions.select
import com.raizlabs.android.dbflow.kotlinextensions.where
import com.raizlabs.android.dbflow.list.FlowCursorList
import com.raizlabs.android.dbflow.sql.language.SQLite
import com.raizlabs.android.dbflow.structure.database.transaction.FastStoreModelTransaction
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LocalGenreDataSource
@Inject constructor(private val dispatchers: AppDispatchers) : LocalDataSource<Genre> {
  override suspend fun deleteAll() = withContext(dispatchers.db) {
    delete(Genre::class).execute()
  }

  override suspend fun saveAll(list: List<Genre>) = withContext(dispatchers.db) {
    val adapter = modelAdapter<Genre>()

    val transaction = FastStoreModelTransaction.insertBuilder(adapter)
      .addAll(list)
      .build()

    database<RemoteDatabase>().executeTransaction(transaction)
  }

  override suspend fun loadAllCursor(): FlowCursorList<Genre> = withContext(dispatchers.db) {
    val query = (select from Genre::class).orderBy(Genre_Table.genre, true)
    return@withContext FlowCursorList.Builder(Genre::class.java).modelQueriable(query).build()

  }

  override suspend fun search(term: String): FlowCursorList<Genre> = withContext(dispatchers.db) {
    val query = (select from Genre::class where Genre_Table.genre.like("%${term.escapeLike()}%"))
      .orderBy(Genre_Table.genre, true)
    return@withContext FlowCursorList.Builder(Genre::class.java).modelQueriable(query).build()
  }

  override suspend fun isEmpty(): Boolean = withContext(dispatchers.db) {
    return@withContext SQLite.selectCountOf().from(Genre::class.java).count() == 0L
  }

  override suspend fun count(): Long = withContext(dispatchers.db) {
    return@withContext SQLite.selectCountOf().from(Genre::class.java).count()
  }
}
