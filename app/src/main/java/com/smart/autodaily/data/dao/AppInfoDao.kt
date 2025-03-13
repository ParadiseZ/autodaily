package com.smart.autodaily.data.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
interface AppInfoDao {

    @Query("update APP_INFO set config_value = :value where id =:id")
    fun updateValueById(id: Int, value : Boolean)

    //未使用
    @Query("select * from APP_INFO where id =:id")
    fun getInfoByInt(id: Int) : Boolean
}