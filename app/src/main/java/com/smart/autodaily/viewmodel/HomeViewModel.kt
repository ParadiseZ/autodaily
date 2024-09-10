package com.smart.autodaily.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.smart.autodaily.api.RemoteApi
import com.smart.autodaily.base.BaseViewModel
import com.smart.autodaily.constant.RunButtonClickResult
import com.smart.autodaily.data.appDb
import com.smart.autodaily.data.entity.ScriptInfo
import com.smart.autodaily.data.entity.request.Request
import com.smart.autodaily.data.entity.resp.Response
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : BaseViewModel(application = application) {
    var refreshing  =  mutableStateOf(false)
    //本地数据
    private val _localScriptList = MutableStateFlow<PagingData<ScriptInfo>>(PagingData.empty())
    val localScriptList: StateFlow<PagingData<ScriptInfo>> get()= _localScriptList
    //到期的数据
    private val _invalidScriptList = MutableStateFlow<List<ScriptInfo>>(emptyList())
    val invalidScriptList : StateFlow<List<ScriptInfo>> get()= _invalidScriptList
    //到期数据循环的当前idx
    private  val _curNeedAcListIdx = MutableStateFlow(0)
    val curNeedAcListIdx :StateFlow<Int> get() = _curNeedAcListIdx
    //是否显示到期数据激活对话框
    private val _showActiveDialogFlag = MutableStateFlow(false)
    val showActiveDialogFlag :StateFlow<Boolean> get()= _showActiveDialogFlag

    init {
        viewModelScope.launch {
            //checkUpdateAll(true)
        }
    }
    //删除数据
    fun deleteScript(sc : ScriptInfo){
        viewModelScope.launch {
            try {
                appDb?.runInTransaction{
                    appDb!!.scriptInfoDao.delete(sc)
                    appDb!!.scriptSetInfoDao.deleteScriptSetInfoByScriptId(sc.scriptId)
                    appDb!!.scriptActionInfoDao.deleteByScriptId(sc.scriptId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun checkBoxClick( index:Int, sc: ScriptInfo){
        //dataList[index] =  sc.copy(checked_flag = !sc.checked_flag)
    }


    //点击运行检测【是否登录】
    fun runButtonClick() : RunButtonClickResult{
        return checkLogin()
    }
    private fun checkLogin() : RunButtonClickResult{
        if (appViewModel.user.value == null){
            return RunButtonClickResult.NOT_LOGIN
        }else{
            return RunButtonClickResult.LOGIN_SUCCESS
        }
    }
    //运行检测【检测需要激活的、可以运行的】

    suspend fun runScriptCheck() : Response<String>{
        val checkedScriptNum = appDb!!.scriptInfoDao.getAllCheckedScript().size
        if(checkedScriptNum==0){
            return Response.error("未选择脚本")
        }
        val request = Request(appViewModel.user.value, checkedScriptNum)
        val checkResult = RemoteApi.runRetrofit.runCheck(request)
        return checkResult
    }
}
