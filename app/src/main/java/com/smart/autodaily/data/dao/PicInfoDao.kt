package com.smart.autodaily.data.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
interface PicInfoDao {
    @Query("SELECT pic_path FROM pic_info where id = :id")
    fun getPicNameById(id : Int): String
}