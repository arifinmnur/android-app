package com.kelsos.mbrc.content.radios

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RadioStationDao {

  @Query("delete from radio_station")
  fun deleteAll()

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertAll(list: List<RadioStationEntity>)

  @Query("select * from radio_station")
  fun getAll(): LiveData<List<RadioStationEntity>>

  @Query("select * from radio_station where name like '%' || :term || '%' ")
  fun search(term: String): LiveData<List<RadioStationEntity>>

  @Query("select count(*) from radio_station")
  fun count(): Long

  @Query("delete from radio_station where date_added != :added")
  fun removePreviousEntries(added: Long)
}
