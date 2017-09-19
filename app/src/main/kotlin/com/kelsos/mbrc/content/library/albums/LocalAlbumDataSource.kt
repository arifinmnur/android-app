package com.kelsos.mbrc.content.library.albums

import com.kelsos.mbrc.RemoteDatabase
import com.kelsos.mbrc.content.library.tracks.Track
import com.kelsos.mbrc.content.library.tracks.Track_Table
import com.kelsos.mbrc.di.modules.AppDispatchers
import com.kelsos.mbrc.extensions.escapeLike
import com.kelsos.mbrc.interfaces.data.LocalDataSource
import com.raizlabs.android.dbflow.kotlinextensions.and
import com.raizlabs.android.dbflow.kotlinextensions.database
import com.raizlabs.android.dbflow.kotlinextensions.delete
import com.raizlabs.android.dbflow.kotlinextensions.from
import com.raizlabs.android.dbflow.kotlinextensions.innerJoin
import com.raizlabs.android.dbflow.kotlinextensions.modelAdapter
import com.raizlabs.android.dbflow.kotlinextensions.on
import com.raizlabs.android.dbflow.kotlinextensions.select
import com.raizlabs.android.dbflow.kotlinextensions.where
import com.raizlabs.android.dbflow.list.FlowCursorList
import com.raizlabs.android.dbflow.sql.language.OperatorGroup.clause
import com.raizlabs.android.dbflow.sql.language.SQLite
import com.raizlabs.android.dbflow.structure.database.transaction.FastStoreModelTransaction
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LocalAlbumDataSource
@Inject
constructor(
  private val dispatchers: AppDispatchers
) : LocalDataSource<Album> {
  override suspend fun deleteAll() = withContext(dispatchers.db) {
    delete(Album::class).execute()
  }

  override suspend fun saveAll(list: List<Album>) = withContext(dispatchers.db) {
    val adapter = modelAdapter<Album>()

    val transaction = FastStoreModelTransaction.insertBuilder(adapter)
      .addAll(list)
      .build()

    database<RemoteDatabase>().executeTransaction(transaction)
  }

  override suspend fun loadAllCursor(): FlowCursorList<Album> = withContext(dispatchers.db) {
    val query = (select from Album::class)
      .orderBy(Album_Table.artist, true)
      .orderBy(Album_Table.album, true)
    return@withContext FlowCursorList.Builder(Album::class.java).modelQueriable(query).build()
  }

  suspend fun getAlbumsByArtist(artist: String): FlowCursorList<Album> =
    withContext(dispatchers.db) {
      val selectAlbum =
        SQLite.select(Album_Table.album.withTable(), Album_Table.artist.withTable()).distinct()
      val artistOrAlbumArtist = clause(Track_Table.artist.withTable().`is`(artist))
        .or(Track_Table.album_artist.withTable().`is`(artist))
      val columns = clause(Track_Table.album.withTable().eq(Album_Table.album.withTable()))
        .and(Track_Table.album_artist.withTable().eq(Album_Table.artist.withTable()))
      val query = (selectAlbum from Album::class
          innerJoin Track::class
          on columns
          where artistOrAlbumArtist)
        .orderBy(Album_Table.artist.withTable(), true)
        .orderBy(Album_Table.album.withTable(), true)
      return@withContext FlowCursorList.Builder(Album::class.java).modelQueriable(query).build()
    }

  override suspend fun search(term: String): FlowCursorList<Album> = withContext(dispatchers.db) {
    val query = (select from Album::class where Album_Table.album.like("%${term.escapeLike()}%"))
    return@withContext FlowCursorList.Builder(Album::class.java).modelQueriable(query).build()
  }

  override suspend fun isEmpty(): Boolean = withContext(dispatchers.db) {
    return@withContext SQLite.selectCountOf().from(Album::class.java).count() == 0L
  }

  override suspend fun count(): Long = withContext(dispatchers.db) {
    return@withContext SQLite.selectCountOf().from(Album::class.java).count()
  }

  suspend fun getAlbumsSorted(
    @Sorting.Fields order: Int,
    ascending: Boolean
  ): FlowCursorList<Album> = withContext(dispatchers.db) {
    val join = SQLite.select().from(Album::class)
      .innerJoin(Track::class)
      .on(
        Album_Table.album.withTable().eq(Track_Table.album.withTable())
          .and(Album_Table.artist.withTable().eq(Track_Table.album_artist.withTable()))
      )

    val sorted = when (order) {
      Sorting.ALBUM -> {
        join.orderBy(Album_Table.album.withTable(), ascending)
      }
      Sorting.ALBUM_ARTIST__ALBUM -> {
        join.orderBy(Album_Table.artist.withTable(), ascending)
          .orderBy(Album_Table.album.withTable(), ascending)
      }
      Sorting.ALBUM_ARTIST__YEAR__ALBUM -> {
        join.orderBy(Album_Table.artist.withTable(), ascending)
          .orderBy(Track_Table.year.withTable(), ascending)
          .orderBy(Album_Table.album.withTable(), ascending)
      }
      Sorting.ARTIST__ALBUM -> {
        join.orderBy(Track_Table.artist.withTable(), ascending)
          .orderBy(Album_Table.album.withTable(), ascending)
      }
      Sorting.GENRE__ALBUM_ARTIST__ALBUM -> {
        join.orderBy(Track_Table.genre.withTable(), ascending)
          .orderBy(Album_Table.artist.withTable(), ascending)
          .orderBy(Album_Table.album.withTable(), ascending)
      }
      Sorting.YEAR__ALBUM -> {
        join.orderBy(Track_Table.year.withTable(), ascending)
          .orderBy(Album_Table.album.withTable(), ascending)
      }
      Sorting.YEAR__ALBUM_ARTIST__ALBUM -> {
        join.orderBy(Track_Table.year.withTable(), ascending)
          .orderBy(Album_Table.artist.withTable(), ascending)
          .orderBy(Album_Table.album.withTable(), ascending)
      }
      else -> throw IllegalArgumentException("no such option")
    }

    return@withContext FlowCursorList.Builder(Album::class.java)
      .modelQueriable(sorted.groupBy(Album_Table.album.withTable(), Album_Table.artist.withTable()))
      .build()
  }
}
