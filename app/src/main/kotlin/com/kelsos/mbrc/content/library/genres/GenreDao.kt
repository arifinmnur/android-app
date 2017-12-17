package com.kelsos.mbrc.content.library.genres

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface GenreDao {

  @Query("delete from genre")
  fun deleteAll()

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun saveAll(list: List<GenreEntity>)

  @Query("select * from genre")
  fun getAll(): DataSource.Factory<Int, GenreEntity>

  @Query("select * from genre where genre like '%' || :term || '%'")
  fun search(term: String): DataSource.Factory<Int, GenreEntity>

  @Query("select count(*) from genre")
  fun count(): Long
}
