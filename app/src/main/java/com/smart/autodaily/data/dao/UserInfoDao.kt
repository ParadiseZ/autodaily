package com.smart.autodaily.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.smart.autodaily.data.entity.UserInfo

@Dao
interface UserInfoDao {
    @Insert
    fun insert(userInfo: UserInfo)

    @Query("select * from user_info where isLogin=1 LIMIT 1")
    fun queryUserInfo() : UserInfo?

    @Update
    fun update(userInfo: UserInfo)
}