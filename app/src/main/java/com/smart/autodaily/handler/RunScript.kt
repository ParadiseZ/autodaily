package com.smart.autodaily.handler

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import com.smart.autodaily.constant.ActionString
import com.smart.autodaily.data.appDb
import com.smart.autodaily.data.entity.ScriptActionInfo
import com.smart.autodaily.data.entity.ScriptInfo
import com.smart.autodaily.data.entity.ScriptSetInfo
import com.smart.autodaily.utils.ShizukuUtil
import com.smart.autodaily.utils.sc.adbClick
import com.smart.autodaily.utils.sc.adbSwap
import com.smart.autodaily.utils.sc.overScript
import com.smart.autodaily.utils.sc.overSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.opencv.core.Mat
import splitties.init.appCtx
import java.io.IOException


object  RunScript {
    private val assetManager: AssetManager = appCtx.assets

    private val _scriptCheckedList = MutableStateFlow<List<ScriptInfo>>(emptyList())
    //val scriptCheckedList : StateFlow<List<ScriptInfo>> = _scriptCheckedList
    //var scriptSetList : List<ScriptInfo> = emptyList()
    //val globalSetList =  MutableStateFlow<List<ScriptSetInfo>>(emptyList())
    private var scriptRunCoroutineScope = CoroutineScope(Dispatchers.IO)
    private var mat = Mat()
    fun runScript() {
        //已选脚本
        scriptRunCoroutineScope.launch {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                runScriptByAdb()
            }
        }

    }

    //shell运行
    private suspend fun runScriptByAdb(){
        _scriptCheckedList.value.forEach{ si->
            if(si.currentRunNum < si.runsMaxNum){
                //当前脚本已选操作
                val scriptActionList = appDb!!.scriptActionInfoDao.getCheckedByScriptId( si.scriptId )
                scriptActionList.forEach {
                    initActionFun(it, si, scriptActionList)
                }
                while (si.currentRunNum < si.runsMaxNum) {
                    val capTime = System.currentTimeMillis()
                    val captureBitmap = ShizukuUtil.iUserService?.execCap("screencap -p")
                    if (captureBitmap != null) {
                        scriptActionList.forEach {
                            if (!it.skipFlag) {
                                //寻找
                                if (it.actionString.startsWith(ActionString.UN_FIND)) {
                                    it.command.forEach { command ->
                                        command.invoke(it)
                                    }
                                }
                            }
                        }
                        if (System.currentTimeMillis() - capTime < 1000) {
                            delay(1000)
                        }
                    }else{
                        println("截图失败！")
                        return
                    }
                }
            }
        }
    }

    private fun execCompet(){

    }

    //操作映射为函数
    private fun initActionFun(scriptActionInfo: ScriptActionInfo, scriptInfo: ScriptInfo, scriptActionList: List<ScriptActionInfo>){
        scriptActionInfo.actionString.split(";").forEach {  action->
            when(action){
                ActionString.CLICK-> scriptActionInfo.command.add { scriptActionInfo.adbClick() }
                ActionString.LONG_CLICK-> scriptActionInfo.command.add { scriptActionInfo.adbSwap() }
                ActionString.FINISH ->{
                    if(scriptInfo.currentRunNum < scriptInfo.runsMaxNum){
                        scriptActionInfo.command.add { scriptActionInfo.overScript { scriptInfo.currentRunNum += 1
                            println("currentRunNum = ${scriptInfo.currentRunNum}")
                        } }
                    }
                }
                action.startsWith(  ActionString.OVER_SET  ).toString() -> {
                    if(action.endsWith(")")){
                        val setId = action.substring(   ActionString.OVER_SET.length, action.length-1   ).toInt()
                        for(element in scriptActionList){
                            if (element.setId == setId){
                                scriptActionInfo.command.add{element.overSet(element)}
                                println("使setId为$setId 停止")
                                break
                            }
                        }
                    }else{
                        scriptActionInfo.command.add { scriptActionInfo.overSet(scriptActionInfo) }
                    }
                }
                action.startsWith(ActionString.STEP).toString() -> {
                    val setId = action.substring(ActionString.STEP.length, action.length-1).toInt()
                    for(element in scriptActionList) {
                        if (element.setId == setId) {
                            scriptActionInfo.command.add { element.overSet(element) }
                            println("使setId为$setId 停止")
                            break
                        }
                    }
                }
            }
        }
    }

    fun initScriptData(scriptList : List<ScriptInfo>){
        this._scriptCheckedList.value = scriptList
    }
    fun stopRunScript(){
        scriptRunCoroutineScope.cancel()
    }

    fun updateScript(scriptInfo: ScriptInfo){
        checkAndRestartScopeState()
        scriptRunCoroutineScope.launch {
            appDb!!.scriptInfoDao.update(scriptInfo)
        }
    }
    fun updateScriptSet(scriptSetInfo: ScriptSetInfo){
        checkAndRestartScopeState()
        scriptRunCoroutineScope.launch {
            appDb!!.scriptSetInfoDao.update(scriptSetInfo)
        }
    }

    private fun checkAndRestartScopeState(){
        if (
            !scriptRunCoroutineScope.isActive
        ){
            scriptRunCoroutineScope = CoroutineScope(Dispatchers.IO)
        }
    }

    fun getPicture(path : String) : Bitmap?{
        try {
            // 获取AssetManager

            // 从assets目录下打开图片
            val inputStream = assetManager.open(path)
            // 将InputStream转化为Bitmap
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close() // 记得关闭InputStream
            return bitmap
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }
}