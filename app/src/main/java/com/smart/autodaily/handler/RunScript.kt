package com.smart.autodaily.handler

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import com.smart.autodaily.command.AdbClick
import com.smart.autodaily.command.AdbSumClick
import com.smart.autodaily.command.Check
import com.smart.autodaily.command.Return
import com.smart.autodaily.command.Skip
import com.smart.autodaily.command.Sleep
import com.smart.autodaily.constant.ActionString
import com.smart.autodaily.data.appDb
import com.smart.autodaily.data.entity.Point
import com.smart.autodaily.data.entity.ScriptActionInfo
import com.smart.autodaily.data.entity.ScriptInfo
import com.smart.autodaily.data.entity.ScriptSetInfo
import com.smart.autodaily.utils.ScreenCaptureUtil
import com.smart.autodaily.utils.ShizukuUtil
import com.smart.autodaily.utils.debug
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
import java.util.Date


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
        _scriptCheckedList.value.forEach scriptForEach@{ si->
            if(si.currentRunNum < si.runsMaxNum){
                val scriptSetInfo = appDb!!.scriptSetInfoDao.getScriptSetByScriptIdLv0(si.scriptId)
                scriptSetInfo.forEach setForEach@ { set->
                    val setIds = appDb!!.scriptSetInfoDao.getScriptSetParentAndChild( set.setId )
                    println("setIds = $setIds")
                    val scriptActionList = appDb!!.scriptActionInfoDao.getCheckedBySetId( setIds, si.scriptId )
                    println("scriptActionList = $scriptActionList")
                    scriptActionList.forEach {
                        initActionFun(it, si, set)
                    }

                    //当前脚本已选操作

                    //while (si.currentRunNum < si.runsMaxNum) {
                    while ( checkIsFinish(set.setId) ) {
                        val capTime = System.currentTimeMillis()
                        val captureBitmap = ShizukuUtil.iUserService?.execCap("screencap -p")
                        if (captureBitmap != null) {
                            scriptActionList.forEach scriptAction@{
                                if (!it.skipFlag) {
                                    //寻找
                                    if (it.actionString.startsWith(ActionString.UN_FIND)) {
                                        it.command.onEach { cmd->
                                            if(cmd is Return){
                                                when(cmd.type){
                                                    ActionString.OVER_SET->{
                                                        appDb!!.scriptSetInfoDao.updateResultFlag(it.setId , true)
                                                    }
                                                    ActionString.FINISH ->{
                                                        if (si.currentRunNum < si.runsMaxNum) {
                                                            si.currentRunNum += 1
                                                            appDb!!.scriptInfoDao.update(si)
                                                        }
                                                        return@scriptForEach
                                                    }
                                                }
                                            } else{
                                                if (    !cmd.exec(it)   ){
                                                    return@scriptAction
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (System.currentTimeMillis() - capTime < 1000) {
                                delay(1000)
                            }
                        }else{
                            debug("截图失败！")
                            return
                        }
                    }
                }

            }else{
                si.currentRunNum = 0
                si.nextRunDate = Date().toString()
                println(si.nextRunDate)
                //appDb!!.scriptInfoDao.update(si)
            }
        }
    }

    //操作映射为函数
    private fun initActionFun(scriptActionInfo: ScriptActionInfo, scriptInfo: ScriptInfo, scriptSetInfo: ScriptSetInfo){
        scriptActionInfo.picId.split(",").forEach {
            debug("setId = ${scriptActionInfo.setId},picId = $it")
            scriptActionInfo.picNameList.add(
                appDb!!.picInfoDao.getPicNameById(it.toInt())
            )
        }
        scriptActionInfo.actionString.split(";").forEach {  action->
            debug("action = ${action}")
            when(action){
                ActionString.CLICK-> scriptActionInfo.command.add (AdbClick())
                ActionString.CLICK_CENTER-> {
                    val point =Point((ScreenCaptureUtil.displayMetrics!!.widthPixels/2).toFloat(),(ScreenCaptureUtil.displayMetrics!!.heightPixels/2).toFloat())
                    scriptActionInfo.command.add (AdbClick(point) )
                }
                ActionString.FINISH ->{
                    scriptActionInfo.command.add (Return(ActionString.FINISH))
                }
                ActionString.SKIP ->{
                    scriptActionInfo.command.add(Skip())
                }
                ActionString.SLEEP -> {
                    scriptActionInfo.command.add(Sleep())
                }
                ActionString.OVER_SET ->{
                    scriptActionInfo.command.add(Return(ActionString.OVER_SET))
                }
                else ->{
                    when{
                        (action.startsWith(  ActionString.CLICK  ) && action.length > 8)  -> {
                            val (x,y) =  action.substring(   ActionString.CLICK.length+1, action.length-1  ).split(",")
                            val point = Point(x.toFloat(),y.toFloat())
                            scriptActionInfo.command.add(AdbSumClick(point))
                            /*scriptActionInfo.command.add{

                                scriptActionInfo.adbClick(scriptActionInfo.point)
                            }*/
                        }
                        action.startsWith(  ActionString.SLEEP  ) -> {
                            val sleepTime = action.substring(   ActionString.SLEEP.length+1, action.length-1   ).toLong()
                            scriptActionInfo.command.add(Sleep(sleepTime))
                        }
                        action.startsWith(  ActionString.CHECK   ) ->{
                            val setId = action.substring(   ActionString.CHECK.length+1, action.length-1   ).toInt()
                            scriptActionInfo.command.add( Check(setId) )
                        }
                        action.startsWith(  ActionString.UN_FIND   ) ->{
                            println(action.substring(   ActionString.UN_FIND.length+1, action.length-1   )+"__SubString")
                            action.substring(   ActionString.UN_FIND.length+1, action.length-1   ).split(",").forEach{
                                scriptActionInfo.picNotFoundList.add(it)
                            }
                        }
                    }
                }
                /*action.startsWith(ActionString.STEP).toString() -> {
                    val setId = action.substring(ActionString.STEP.length, action.length-1).toInt()
                    for(element in scriptActionList) {
                        if (element.setId == setId) {
                            println("使setId为$setId 停止")
                            break
                        }
                    }
                }*/
            }
        }
    }

    private fun checkIsFinish(setId : Int) : Boolean{
        return appDb!!.scriptSetInfoDao.getChildResultFlag(setId)
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