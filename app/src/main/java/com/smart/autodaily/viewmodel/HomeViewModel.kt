package com.smart.autodaily.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.smart.autodaily.base.BaseViewModel
import com.smart.autodaily.data.appDb
import com.smart.autodaily.data.entity.ScriptInfo
import com.smart.autodaily.utils.ScreenUtil

class HomeViewMode(application: Application) : BaseViewModel(application = application) {
    var refreshing  =  mutableStateOf(false)
    /*
    https://developer.android.google.cn/codelabs/basic-android-kotlin-compose-viewmodel-and-state?hl=zh-cn#4
    private val _uiState = MutableStateFlow( list )//在 Android 中，StateFlow 适用于必须维护可观察的不可变状态的类
    val uiState : StateFlow<List<ScriptInfo>> = _uiState.asStateFlow()
    */

    val dataList = mutableStateListOf(
        ScriptInfo(uid=1, script_id=1, script_name="崩坏3：官服", script_version="24.4.81", last_version="24.4.81", checked_flag=false, expiration_time="2024-12-12 08:04:00", package_name="com.mihoyo.bh3", runsMaxNum=2, next_run_date="2024-04-08 16:00:00", download_time="2024-04-08 08:24:00") ,
        ScriptInfo(uid=2, script_id=2, script_name="天下布魔：工口服", script_version="24.4.81", last_version="24.4.81", checked_flag=false, expiration_time="2024-12-12 08:27:00", package_name="com.pinkcore.tkfm", runsMaxNum=2, next_run_date="2024-04-07 16:00:00", download_time="2024-04-08 08:24:00"),
    ScriptInfo(uid=3, script_id=3, script_name="公主连结：台服", script_version="0.0.1", last_version="0.0.1", checked_flag=false, expiration_time="2024-12-24 08:27:00", package_name="com.pinkcore.tkfm", runsMaxNum=2, next_run_date="2024-04-07 16:00:00", download_time="2024-04-08 08:24:00"),
    ScriptInfo(uid=4, script_id=4, script_name="天下布魔：工口服", script_version="0.0.1", last_version="0.0.1", checked_flag=false, expiration_time="2024-12-12 08:27:00", package_name="com.pinkcore.tkfm", runsMaxNum=2, next_run_date="2024-04-07 16:00:00", download_time="2024-04-08 08:24:00"),
    ScriptInfo(uid=5, script_id=5, script_name="公主连结：台服", script_version="0.0.1", last_version="0.0.1", checked_flag=false, expiration_time="2024-12-24 08:27:00", package_name="com.pinkcore.tkfm", runsMaxNum=2, next_run_date="2024-04-07 16:00:00", download_time="2024-04-08 08:24:00"),
    ScriptInfo(uid=6, script_id=6, script_name="天下布魔：工口服", script_version="0.0.1", last_version="0.0.1", checked_flag=false, expiration_time="2024-12-12 08:27:00", package_name="com.pinkcore.tkfm", runsMaxNum=2, next_run_date="2024-04-07 16:00:00", download_time="2024-04-08 08:24:00"),
    ScriptInfo(uid=7, script_id=7, script_name="公主连结：台服", script_version="0.0.1", last_version="0.0.1", checked_flag=false, expiration_time="2024-12-24 08:27:00", package_name="com.pinkcore.tkfm", runsMaxNum=2, next_run_date="2024-04-07 16:00:00", download_time="2024-04-08 08:24:00"),
    ScriptInfo(uid=8, script_id=8, script_name="天下布魔：工口服", script_version="0.0.1", last_version="0.0.1", checked_flag=false, expiration_time="2024-12-12 08:27:00", package_name="com.pinkcore.tkfm", runsMaxNum=2, next_run_date="2024-04-07 16:00:00", download_time="2024-04-08 08:24:00"),
    ScriptInfo(uid=9, script_id=9, script_name="公主连结：台服", script_version="0.0.1", last_version="0.0.1", checked_flag=false, expiration_time="2024-12-24 08:27:00", package_name="com.pinkcore.tkfm", runsMaxNum=2, next_run_date="2024-04-07 16:00:00", download_time="2024-04-08 08:24:00"),
    ScriptInfo(uid=10, script_id=10, script_name="游戏脚本10", script_version="0.0.1", last_version="0.0.1", checked_flag=false, expiration_time="2024-07-13 07:38:51", package_name="com.example.game10", runsMaxNum=8, next_run_date="2024-07-01 16:00:00", download_time="2024-06-06 21:26:54")
/*        ScriptInfo(uid = 1, gameId = 1, gameName = "崩坏3：官服", scriptVersion = "0.1", endTime = "2024-04-02"),
        ScriptInfo(uid = 2, gameId = 2, gameName = "天下布魔：工口服", scriptVersion = "0.1", endTime = "2024-05-02"),
        ScriptInfo(uid = 3, gameId = 3, gameName = "魔法纪录：日服", scriptVersion = "0.1", endTime = "2024-04-06")*/
    )

    //val dataList = mutableStateListOf<ScriptInfo>()
    suspend fun loadLocalScriptInfo() : List<ScriptInfo> {
        appDb?.scriptInfoDao?.getScriptInfoFlow()
        return dataList
    }

    fun onScriptListRemove(sc : ScriptInfo){
        dataList.remove(sc)
    }

    fun onScriptListSet(){
        val list = dataList.toList()
        dataList.clear()
        dataList.addAll( list)
    }

    fun checkBoxClick( index:Int, sc: ScriptInfo){
        dataList[index] =  sc.copy(checked_flag = !sc.checked_flag)
    }

    fun smallRunButtonClick( index:Int ){
        if ( dataList[index].checked_flag ){
            ScreenUtil.showMsg(" 已选择："+ dataList[index].script_name, context)
        }
    }

    fun runButtonClick(ct : Context){
        ScreenUtil.showMsg(" 数据量："+ dataList.size, ct)
        dataList.forEach {
            if ( it.checked_flag ){
                ScreenUtil.showMsg(" 已选择："+ it.script_name, ct)
            }
        }
    }
}
