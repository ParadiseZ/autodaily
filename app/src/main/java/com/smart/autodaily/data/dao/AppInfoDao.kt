package com.smart.autodaily.data.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
interface AppInfoDao {
    @Query("SELECT COUNT(1) FROM APP_INFO WHERE id in (1,2) and value=0")
    fun getPrivacyRes() : Int

    @Query("update APP_INFO set value = :value where id =:id")
    fun updateValueById(id: Int, value : Boolean)

    //未使用
    @Query("select * from APP_INFO where id =:id")
    fun getInfoByInt(id: Int) : Boolean
}