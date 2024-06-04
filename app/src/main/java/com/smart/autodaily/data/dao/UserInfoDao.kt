package com.smart.autodaily.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.smart.autodaily.data.entity.UserInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface UserInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(userInfo: UserInfo)

    @Query("select * from user_info where isLogin=1 LIMIT 1")
    fun queryUserInfo() : Flow<UserInfo?>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(userInfo: UserInfo)

    @Delete
    fun delete(userInfo: UserInfo)
}