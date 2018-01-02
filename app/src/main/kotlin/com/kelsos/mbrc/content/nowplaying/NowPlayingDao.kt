package com.kelsos.mbrc.content.nowplaying

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface NowPlayingDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertAll(list: List<NowPlayingEntity>)

  @Query("delete from now_playing")
  fun deleteAll()

  @Query("select * from now_playing")
  fun getAll(): LiveData<List<NowPlayingEntity>>

  @Query("select * from now_playing where title like '%' || :term || '%' or artist like '%' || :term || '%'")
  fun search(term: String): LiveData<List<NowPlayingEntity>>

  @Query("select count(*) from now_playing")
  fun count(): Long

  @Query("delete from now_playing where date_added != :added")
  fun removePreviousEntries(added: Long)

}
