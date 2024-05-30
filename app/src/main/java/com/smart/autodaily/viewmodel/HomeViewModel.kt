package com.smart.autodaily.viewmodel

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.smart.autodaily.api.RemoteApi
import com.smart.autodaily.base.BaseViewModel
import com.smart.autodaily.constant.RunButtonClickResult
import com.smart.autodaily.data.appDb
import com.smart.autodaily.data.entity.ScriptInfo
import com.smart.autodaily.data.entity.UserInfo
import com.smart.autodaily.data.entity.request.Request
import com.smart.autodaily.data.entity.resp.Response
import com.smart.autodaily.utils.ExceptionUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(application: Application) : BaseViewModel(application = application) {
    var refreshing  =  mutableStateOf(false)
    var userInfo : MutableState<UserInfo?> ?=null
    //本地数据
    private val _localScriptList = MutableStateFlow<PagingData<ScriptInfo>>(PagingData.empty())
    val localScriptList: StateFlow<PagingData<ScriptInfo>> = _localScriptList

    //检测更新
    private val _checkUpdateFlagFlow = MutableStateFlow(false)
    val checkUpdateFlagFlow: StateFlow<Boolean> get() = _checkUpdateFlagFlow

    /*
    https://developer.android.google.cn/codelabs/basic-android-kotlin-compose-viewmodel-and-state?hl=zh-cn#4
    private val _uiState = MutableStateFlow( list )//在 Android 中，StateFlow 适用于必须维护可观察的不可变状态的类
    val uiState : StateFlow<List<ScriptInfo>> = _uiState.asStateFlow()
    */

   /* val dataList = mutableStateListOf(
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
*//*        ScriptInfo(uid = 1, gameId = 1, gameName = "崩坏3：官服", scriptVersion = "0.1", endTime = "2024-04-02"),
        ScriptInfo(uid = 2, gameId = 2, gameName = "天下布魔：工口服", scriptVersion = "0.1", endTime = "2024-05-02"),
        ScriptInfo(uid = 3, gameId = 3, gameName = "魔法纪录：日服", scriptVersion = "0.1", endTime = "2024-04-06")*//*
    )*/

    //val dataList = mutableStateListOf<ScriptInfo>()

    //删除数据
    fun deleteScript(sc : ScriptInfo){
        viewModelScope.launch {
            try {
                appDb!!.scriptInfoDao.delete(sc)
                appDb!!.scriptSetInfoDao.deleteScriptSetInfoByScriptId(sc.scriptId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    //检测更新

    suspend fun checkUpdateAll(loadDataFlag : Boolean){
        withContext(Dispatchers.IO){
            try {
                // 获取本地脚本列表，并转成ID和版本号的Map
                val localScriptListMap = appDb!!.scriptInfoDao.getIdAndLastVer()
                    .associate { it.scriptId to it.lastVersion }
                // 调用远程接口检查更新
                val remoteScriptList = RemoteApi.searchDownRetrofit.checkUpdateByIdAndVer(localScriptListMap)
                // 更新本地数据库中的脚本版本信息
                remoteScriptList.data?.forEach {
                    appDb!!.scriptInfoDao.updateLastVerById(it.key, it.value)
                }
                // 重新加载本地数据
            }catch (e:Exception){
                e.message
            }

        }
    }
    fun checkBoxClick( index:Int, sc: ScriptInfo){
        //dataList[index] =  sc.copy(checked_flag = !sc.checked_flag)
    }


    fun runButtonClick() : RunButtonClickResult{
        return checkLogin()
    }
    private fun checkLogin() : RunButtonClickResult{
        if (userInfo==null){
            userInfo = mutableStateOf(
                appDb!!.userInfoDao.queryUserInfo()
            )
        }
        userInfo?.value?.let {
            return RunButtonClickResult.LOGIN_SUCCESS
        }?:let{
            return RunButtonClickResult.NOT_LOGIN
        }
    }

    suspend fun runScriptCheck() : Response<List<Int>>{
        val userInfo = userInfo?.value
        val checkedScriptIds = appDb!!.scriptInfoDao.getAllCheckedScript()
        val request = Request(userInfo, checkedScriptIds)
        val checkResultList = ExceptionUtil.tryCatchList(
            tryFun = RemoteApi.runRetrofit.runCheck(request),
            exceptionMsg = "运行失败！"
        )
        return checkResultList
    }
}
