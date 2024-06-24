package com.smart.autodaily.handler

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.smart.autodaily.data.appDb
import com.smart.autodaily.data.entity.ScriptInfo
import com.smart.autodaily.data.entity.ScriptSetInfo
import com.smart.autodaily.utils.ShizukuUtil
import com.smart.autodaily.utils.cv.CaptureHandler
import com.smart.autodaily.utils.sc.AsClick
import com.smart.autodaily.utils.sc.AsLongClick
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.opencv.core.Mat
import splitties.init.appCtx
import java.io.IOException


object  RunScript {
    private val assetManager: AssetManager = appCtx.assets

    private val _scriptCheckedList = MutableStateFlow<List<ScriptInfo>>(emptyList())
    val scriptCheckedList : StateFlow<List<ScriptInfo>> = _scriptCheckedList
    var scriptSetList : List<ScriptInfo> = emptyList()
    val globalSetList =  MutableStateFlow<List<ScriptSetInfo>>(emptyList())
    private var scriptRunCoroutineScope = CoroutineScope(Dispatchers.IO)
    private var mat = Mat()

    fun runScript() {
        _scriptCheckedList.value.forEach{ si->
            val scriptActionList = appDb!!.scriptActionInfoDao.getCheckedByScriptId( si.scriptId )
            scriptActionList.forEach {
                it.actionString.split(",").forEach { action->
                    when(action){
                        "click"-> it.command.add { it.AsClick() }
                        "longClick"-> it.command.add { it.AsLongClick() }
                    }
                }
            }
            while (true){
                val captureBitmap  = ShizukuUtil.iUserService?.execCap("screencap -p")
                if (captureBitmap != null) {
                    scriptActionList.forEach {
                        it.command.forEach { command->
                            command.invoke(it)
                        }
                    }
                }
            }
        }
        println("进入runScript")
        //val bitMapRun  = ShizukuUtil.iUserService?.execLine("screencap -p /storage/emulated/0/Pictures/${name}.png")
        val bitMapRun  = ShizukuUtil.iUserService?.execCap("screencap -p")

        //println(ScreenCaptureUtil.mps)
        if (bitMapRun != null) {
            //ScreenCaptureUtil.saveScreenCapture(bitMapRun)
            CaptureHandler.saveCaptureMat(bitMapRun, mat)
            println("大小：${mat.size()}")
        }
        println(bitMapRun)
    }

    fun initScriptData(scriptList : List<ScriptInfo>){
        this._scriptCheckedList.value = scriptList
    }

    fun initActionData(){

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