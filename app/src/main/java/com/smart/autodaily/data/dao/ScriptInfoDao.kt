package com.smart.autodaily.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.smart.autodaily.data.entity.ScriptInfo
import com.smart.autodaily.data.entity.request.ScriptInfoCheckUpdate
import kotlinx.coroutines.flow.Flow

@Dao
interface ScriptInfoDao {
    @Query("SELECT * FROM Script_Info where current_status!=2 and script_id>0 ORDER BY script_id LIMIT :pageSize OFFSET :starIndex")
    fun getScriptInfoByPage(pageSize : Int, starIndex : Int) : List<ScriptInfo>

    @Query("SELECT * FROM Script_Info where current_status!=2 and script_id>0 and script_name like  :scriptName || '%' ORDER BY script_id LIMIT :pageSize OFFSET :starIndex")
    fun getScriptInfoByPage(scriptName : String,pageSize : Int, starIndex : Int) : List<ScriptInfo>

    @Query("SELECT script_id FROM Script_Info where  current_status!=2 and script_id>0 ORDER BY script_id")
    fun getScriptIdAll() : List<Int>

    @Query("SELECT script_id FROM Script_Info where  current_status!=2 and script_id>0 and checked_flag = 1")
    fun getAllCheckedScript() : List<Int>

    @Query("SELECT * FROM Script_Info where  current_status!=2  and script_id>0 and checked_flag = 1 and (next_run_date is null or next_run_date <= Date())")
    fun getAllScriptByChecked() : List<ScriptInfo>

    @Query("SELECT script_id,script_version FROM Script_Info")
    fun getIdAndVersion() : List<ScriptInfoCheckUpdate>

    @Query("SELECT * FROM Script_Info WHERE script_id = :scriptId")
    fun getScriptInfoByScriptId(scriptId : Int) : ScriptInfo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert( scriptInfo: ScriptInfo )

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update( scriptInfo: ScriptInfo )

    @Delete
    fun delete( scriptInfo: ScriptInfo )

    @Query("delete from Script_Info where script_id = :scriptId")
    fun deleteByScriptId(scriptId: Int )

    @Query("update Script_Info set last_version = :lastVer,current_status = :currentStatus,need_app_update = :needAppUpdate where script_id = :scriptId")
    fun updateLastVerById(scriptId: Int , lastVer : Int, currentStatus : Int , needAppUpdate : Int)

    @Query("select * from Script_Info where current_status!=2 and script_id>0")
    fun getLocalScriptAll() : Flow<List<ScriptInfo>>

    @Query("select * from Script_Info where script_id in (:scriptIds) and  current_status!=2")
    fun getLocalScriptByIds(scriptIds: List<Int>) : Flow<List<ScriptInfo>>
}