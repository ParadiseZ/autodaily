package com.smart.autodaily.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.smart.autodaily.base.BaseViewModel
import com.smart.autodaily.data.AppDb
import com.smart.autodaily.data.appDb
import com.smart.autodaily.data.entity.ScriptInfo
import com.smart.autodaily.data.repository.ScriptInfoRepository
import com.smart.autodaily.utils.ScreenUtil

class HomeViewMode(application: Application) : BaseViewModel(application = application) {
    var refreshing  =  mutableStateOf(false)
    /*
    https://developer.android.google.cn/codelabs/basic-android-kotlin-compose-viewmodel-and-state?hl=zh-cn#4
    private val _uiState = MutableStateFlow( list )//在 Android 中，StateFlow 适用于必须维护可观察的不可变状态的类
    val uiState : StateFlow<List<ScriptInfo>> = _uiState.asStateFlow()
    */
    /*
    val dataList = mutableStateListOf(
        ScriptInfo(uid = 1, gameId = 1, gameName = "崩坏3：官服", scriptVersion = "0.1", endTime = "2024-04-02"),
        ScriptInfo(uid = 2, gameId = 2, gameName = "天下布魔：工口服", scriptVersion = "0.1", endTime = "2024-05-02"),
        ScriptInfo(uid = 3, gameId = 3, gameName = "魔法纪录：日服", scriptVersion = "0.1", endTime = "2024-04-06")
    )
    */
    val dataList = mutableStateListOf<ScriptInfo>()
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
        dataList[index] =  sc.copy(checkedFlag = !sc.checkedFlag)
    }

    fun smallRunButtonClick( index:Int ){
        if ( dataList[index].checkedFlag ){
            ScreenUtil.showMsg(" 已选择："+ dataList[index].scriptName, context)
        }
    }

    fun runButtonClick(ct : Context){
        ScreenUtil.showMsg(" 数据量："+ dataList.size, ct)
        dataList.forEach {
            if ( it.checkedFlag ){
                ScreenUtil.showMsg(" 已选择："+ it.scriptName, ct)
            }
        }
    }
}
