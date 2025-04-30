package com.smart.autodaily.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.smart.autodaily.api.RemoteApi
import com.smart.autodaily.base.BaseViewModel
import com.smart.autodaily.constant.MODEL_BIN
import com.smart.autodaily.constant.MODEL_PARAM
import com.smart.autodaily.data.appDb
import com.smart.autodaily.data.entity.ScriptInfo
import com.smart.autodaily.data.entity.request.Request
import com.smart.autodaily.data.entity.resp.Response
import com.smart.autodaily.utils.deleteFile
import kotlinx.coroutines.launch
import splitties.init.appCtx
import java.io.File

class HomeViewModel(application: Application) : BaseViewModel(application = application) {
    init {
        viewModelScope.launch {
            //checkUpdateAll(true)
        }
    }
    //删除数据
    fun deleteScript(sc : ScriptInfo){
        try {
            appDb.runInTransaction{
                //appDb.scriptInfoDao.deleteByScriptId(sc.scriptId)
                //appDb.scriptSetInfoDao.deleteScriptSetInfoByScriptId(sc.scriptId)
                appDb.scriptActionInfoDao.deleteByScriptId(sc.scriptId)
                val externalParamFile = File(appCtx.getExternalFilesDir("") , sc.modelPath+"/"+ MODEL_PARAM)
                val externalBinFile = File(appCtx.getExternalFilesDir("") , sc.modelPath+"/"+ MODEL_BIN)
                deleteFile(externalBinFile)
                deleteFile(externalParamFile)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun checkBoxClick( index:Int, sc: ScriptInfo){
        //dataList[index] =  sc.copy(checked_flag = !sc.checked_flag)
    }

    suspend fun deleteRunStatus(si : ScriptInfo){
        appDb.scriptRunStatusDao.deleteStatus(si.scriptId)
        appDb.scriptSetRunStatusDao.deleteStatus(si.scriptId)
    }

    //检测可运行数量
    suspend fun runScriptCheck() : Response<String>{
        val checkedScript = appDb.scriptInfoDao.getAllCheckedScript()
        if(checkedScript.isEmpty()){
            return Response.error("未选择脚本")
        }
        val request = Request(appViewModel.user.value, checkedScript)
        return RemoteApi.runRetrofit.runCheck(request)
    }
}
