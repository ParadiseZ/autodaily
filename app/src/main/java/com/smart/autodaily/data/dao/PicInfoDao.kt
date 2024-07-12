package com.smart.autodaily.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.smart.autodaily.data.entity.PicInfo

@Dao
interface PicInfoDao {
    @Query("SELECT pic_path FROM pic_info where id = :id")
    fun getPicNameById(id : Int): String

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(picInfoList: List<PicInfo>)
}