package com.smart.autodaily.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.smart.autodaily.data.entity.LabelTemp

@Dao
interface LabelTempDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert( labels: List<LabelTemp>)

    @Query("delete from label_temp")
    fun deleteData()

    @Query("UPDATE sqlite_sequence SET seq = 0 WHERE name = 'label_temp'")
    fun resetSeq()
}