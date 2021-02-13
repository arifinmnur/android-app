package com.kelsos.mbrc.repository.data

import com.kelsos.mbrc.data.db.RemoteDatabase
import com.kelsos.mbrc.data.library.Artist
import com.kelsos.mbrc.data.library.Artist_Table.artist
import com.kelsos.mbrc.data.library.Track
import com.kelsos.mbrc.data.library.Track_Table
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
import javax.inject.Inject

class LocalArtistDataSourceImpl
@Inject constructor(private val dispatchers: AppDispatchers) : LocalArtistDataSource {

  override suspend fun deleteAll() = withContext(dispatchers.db) {
    delete(Artist::class).execute()
  }

  override suspend fun saveAll(list: List<Artist>) {
    val adapter = modelAdapter<Artist>()

    val transaction = FastStoreModelTransaction.insertBuilder(adapter)
      .addAll(list)
      .build()

    database<RemoteDatabase>().executeTransaction(transaction)
  }

  override suspend fun loadAllCursor(): FlowCursorList<Artist> = withContext(dispatchers.db) {
    val query = (select from Artist::class).orderBy(artist, true)
    return@withContext FlowCursorList.Builder(Artist::class.java).modelQueriable(query).build()
  }

  override suspend fun getArtistByGenre(genre: String): FlowCursorList<Artist> =
    withContext(dispatchers.db) {
      val query = SQLite.select().distinct()
        .from(Artist::class.java)
        .innerJoin(Track::class.java)
        .on(
          artist.withTable()
            .eq(Track_Table.artist.withTable())
        )
        .where(Track_Table.genre.`is`(genre))
        .orderBy(artist.withTable(), true).groupBy(artist.withTable())
      return@withContext FlowCursorList.Builder(Artist::class.java).modelQueriable(query).build()
    }

  override suspend fun search(term: String): FlowCursorList<Artist> = withContext(dispatchers.db) {
    val query = (select from Artist::class where artist.like("%${term.escapeLike()}%"))
    return@withContext FlowCursorList.Builder(Artist::class.java).modelQueriable(query).build()
  }

  override suspend fun getAlbumArtists(): FlowCursorList<Artist> = withContext(dispatchers.db) {
    val query = SQLite.select().distinct()
      .from(Artist::class.java)
      .innerJoin(Track::class.java)
      .on(artist.withTable().eq(Track_Table.artist.withTable()))
      .where(artist.withTable().`in`(Track_Table.album_artist.withTable()))
      .orderBy(artist.withTable(), true).groupBy(artist.withTable())
    return@withContext FlowCursorList.Builder(Artist::class.java).modelQueriable(query).build()
  }

  override suspend fun isEmpty(): Boolean = withContext(dispatchers.db) {
    return@withContext SQLite.selectCountOf().from(Artist::class.java).count() == 0L
  }

  override suspend fun count(): Long = withContext(dispatchers.db) {
    return@withContext SQLite.selectCountOf().from(Artist::class.java).count()
  }
}
