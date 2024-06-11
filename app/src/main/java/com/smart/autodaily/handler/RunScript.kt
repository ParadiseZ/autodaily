package com.smart.autodaily.handler

import com.smart.autodaily.data.appDb
import com.smart.autodaily.data.entity.ScriptInfo
import com.smart.autodaily.data.entity.ScriptSetInfo
import com.smart.autodaily.utils.ScreenCaptureUtil
import com.smart.autodaily.utils.ToastUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

object  RunScript {
    val scriptList = MutableStateFlow<List<ScriptInfo>>(emptyList())//homeViewModel->AppViewModel初始化
    var scriptSetList : List<ScriptInfo> = emptyList()
    val globalSetList =  MutableStateFlow<List<ScriptSetInfo>>(emptyList())
    private val scriptRunCoroutineScope = CoroutineScope(Dispatchers.IO)

    fun runScript() {
        ScreenCaptureUtil.handlerThread.start()
        scriptRunCoroutineScope.launch {
            try {
                ScreenCaptureUtil.screenCapture()

            }catch (e: RuntimeException){
                e.printStackTrace()
                ScreenCaptureUtil.accessibilityService?.let { ToastUtil.showLong(it.applicationContext, "运行失败！") }
            }
        }
    }

    fun initActionData(){

    }

    fun stopRunScript(){
        ScreenCaptureUtil.handlerThread.quitSafely()
        scriptRunCoroutineScope.cancel()
    }

    fun updateScript(scriptInfo: ScriptInfo){
        scriptRunCoroutineScope.launch {
            appDb!!.scriptInfoDao.update(scriptInfo)
        }
    }
    fun updateScriptSet(scriptSetInfo: ScriptSetInfo){
        scriptRunCoroutineScope.launch {
            appDb!!.scriptSetInfoDao.update(scriptSetInfo)
        }
    }
}