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
import com.smart.autodaily.utils.ExceptionUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
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
                appDb!!.scriptInfoDao.delete(sc)
                appDb!!.scriptSetInfoDao.deleteScriptSetInfoByScriptId(sc.scriptId)
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

    suspend fun runScriptCheck() : Response<List<Int>>{
        val checkedScriptIds = appDb!!.scriptInfoDao.getAllCheckedScript()
        if(checkedScriptIds.isEmpty()){
            return Response.error("未选择脚本")
        }
        val request = Request(appViewModel.user.value, checkedScriptIds)
        val checkResultList = ExceptionUtil.tryCatchList(
            tryFun = RemoteApi.runRetrofit.runCheck(request),
            exceptionMsg = "运行失败！"
        )
        if(checkResultList.code==200){
            //更新未到期的内容
            checkResultList.normalData?.map {
                appDb!!.scriptInfoDao.updateExpirationTimeByScriptId(it.key, it.value)
            }
            checkResultList.officialData?.let {
                //赋值数据
                appDb!!.scriptInfoDao.getLocalScriptByIds(it).collectLatest {
                    _invalidScriptList.value = it
                }
                //从0开始
                _curNeedAcListIdx.value = 0
                _showActiveDialogFlag.value =true
            }
        }
        return checkResultList
    }

    //激活脚本
    suspend fun  activeScriptById(index : Int) : Response<String>{
        if(appViewModel.user.value?.canActivateNum!! <=0){
            _showActiveDialogFlag.value = false
            return Response.error("可激活次数不足！")
        }
        val request:Request<Int> = Request(appViewModel.user.value, _invalidScriptList.value[index].scriptId)
        val res = ExceptionUtil.tryCatch(
            tryFun = RemoteApi.runRetrofit.activeScript(request),
            exceptionMsg = "激活失败！"
        )
        if (res.code==200){
            appViewModel.user.value?.canActivateNum = appViewModel.user.value?.canActivateNum?.minus(1)
            appViewModel.user.value?.let { appDb!!.userInfoDao.update(it) }
            _invalidScriptList.value[index].expirationTime = res.data
            appViewModel.updateScript(_invalidScriptList.value[index])
            dialogController()
        }else{
            _showActiveDialogFlag.value = false
        }
        return res
    }

    //取消激活
    fun cancelActiveScript(curIndex:Int){
        viewModelScope.launch {
            _invalidScriptList.value[curIndex].checkedFlag = false
            appViewModel.updateScript(_invalidScriptList.value[curIndex])
            if (curIndex<_invalidScriptList.value.size){
                _curNeedAcListIdx.value += 1
                dialogController()
            }else{
                _showActiveDialogFlag.value = false
            }
        }
    }

    private fun dialogController(){
        viewModelScope.launch {
            _showActiveDialogFlag.value = false
            delay(1000)
            _showActiveDialogFlag.value = true
        }
    }
}
