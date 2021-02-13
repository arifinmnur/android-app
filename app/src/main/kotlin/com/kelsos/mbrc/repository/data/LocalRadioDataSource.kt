package com.kelsos.mbrc.repository.data

import com.kelsos.mbrc.data.RadioStation
import com.kelsos.mbrc.data.RadioStation_Table
import com.kelsos.mbrc.di.modules.AppDispatchers
import com.kelsos.mbrc.extensions.escapeLike
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

class LocalRadioDataSource(val dispatchers: AppDispatchers) : LocalDataSource<RadioStation> {
  override suspend fun deleteAll() = withContext(dispatchers.db) {
    delete(RadioStation::class).execute()
  }

  override suspend fun saveAll(list: List<RadioStation>) = withContext(dispatchers.db) {
    val adapter = modelAdapter<RadioStation>()

    val transaction = FastStoreModelTransaction.insertBuilder(adapter)
      .addAll(list)
      .build()

    database<RadioStation>().executeTransaction(transaction)
  }

  override suspend fun loadAllCursor(): FlowCursorList<RadioStation> = withContext(dispatchers.db) {
    val modelQueriable = (select from RadioStation::class)
    return@withContext FlowCursorList.Builder(RadioStation::class.java)
      .modelQueriable(modelQueriable).build()
  }

  override suspend fun search(term: String): FlowCursorList<RadioStation> =
    withContext(dispatchers.db) {
      val modelQueriable =
        (select from RadioStation::class where RadioStation_Table.name.like("%${term.escapeLike()}%"))
      return@withContext FlowCursorList.Builder(RadioStation::class.java)
        .modelQueriable(modelQueriable).build()
    }

  override suspend fun isEmpty(): Boolean = withContext(dispatchers.db){
    return@withContext SQLite.selectCountOf().from(RadioStation::class.java).count() == 0L
  }

  override suspend fun count(): Long = withContext(dispatchers.db){
    return@withContext SQLite.selectCountOf().from(RadioStation::class.java).count()
  }
}
