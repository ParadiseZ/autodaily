package com.smart.autodaily.handler

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import com.smart.autodaily.constant.ActionString
import com.smart.autodaily.data.appDb
import com.smart.autodaily.data.entity.Point
import com.smart.autodaily.data.entity.ScriptActionInfo
import com.smart.autodaily.data.entity.ScriptInfo
import com.smart.autodaily.data.entity.ScriptSetInfo
import com.smart.autodaily.utils.ScreenCaptureUtil
import com.smart.autodaily.utils.ShizukuUtil
import com.smart.autodaily.utils.debug
import com.smart.autodaily.utils.sc.adbClick
import com.smart.autodaily.utils.sc.check
import com.smart.autodaily.utils.sc.overScript
import com.smart.autodaily.utils.sc.overSet
import com.smart.autodaily.utils.sc.skip
import com.smart.autodaily.utils.sc.sleep
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
        _scriptCheckedList.value.forEach{ si->
            if(si.currentRunNum < si.runsMaxNum){
                val scriptSetInfo = appDb!!.scriptSetInfoDao.getScriptSetByScriptIdLv0(si.scriptId)
                scriptSetInfo.forEach{ set->
                    val setIds = appDb!!.scriptSetInfoDao.getScriptSetParentAndChild( set.setId )
                    println("setIds = $setIds")
                    val scriptActionList = appDb!!.scriptActionInfoDao.getCheckedBySetId( setIds, si.scriptId )
                    println("scriptActionList = $scriptActionList")
                    scriptActionList.forEach {
                        it.picPath = si.picPath.toString()
                        initActionFun(it, si, set)
                    }

                    //当前脚本已选操作

                    //while (si.currentRunNum < si.runsMaxNum) {
                    while ( !set.resultFlag ) {
                        val capTime = System.currentTimeMillis()
                        val captureBitmap = ShizukuUtil.iUserService?.execCap("screencap -p")
                        if (captureBitmap != null) {
                            scriptActionList.forEach {
                                if (!it.skipFlag) {
                                    //寻找
                                    if (it.actionString.startsWith(ActionString.UN_FIND)) {
                                        for( command in it.command){
                                            if (command.invoke(it).skipFlag) {
                                                break
                                            }
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

            }else{
                si.currentRunNum = 0
                si.nextRunDate = Date().toString()
                appDb!!.scriptInfoDao.update(si)
            }
        }
    }

    private fun execCompet(){

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
                ActionString.CLICK-> scriptActionInfo.command.add { scriptActionInfo.adbClick() }
                ActionString.CLICK_CENTER-> {
                    val point =Point((ScreenCaptureUtil.displayMetrics!!.widthPixels/2).toFloat(),(ScreenCaptureUtil.displayMetrics!!.heightPixels/2).toFloat())
                    scriptActionInfo.command.add { scriptActionInfo.adbClick(point) }
                }
                (action.startsWith(  ActionString.CLICK  )  && action.length>8).toString() -> {
                    val pointArrHand =  action.substring(   ActionString.CLICK.length, action.length-1  ).split(",")
                    println("pointArrHand = ${pointArrHand}")
                    val pointArr =  pointArrHand.toString().replace("+","").replace("-","")
                    println("pointArr = ${pointArr}")
                    scriptActionInfo.command.add{
                        scriptActionInfo.point?.let { p->
                            if(pointArrHand[0].startsWith("+")){
                                p.x += pointArr[0].code.toFloat()
                            }else if(pointArrHand[0].startsWith("-")){
                                p.x -=pointArr[0].code.toFloat()
                            }
                            if (pointArrHand[1].startsWith("+")){
                                p.y += pointArr[1].code.toFloat()
                            }else if(pointArrHand[1].startsWith("-")){
                                p.y -= pointArr[1].code.toFloat()
                            }
                        }
                        scriptActionInfo.adbClick(scriptActionInfo.point)
                    }
                }

                ActionString.FINISH ->{
                    if(scriptInfo.currentRunNum < scriptInfo.runsMaxNum){
                        scriptActionInfo.command.add { scriptActionInfo.overScript {
                            scriptInfo.currentRunNum += 1
                            appDb!!.scriptInfoDao.update(scriptInfo)
                            println("currentRunNum = ${scriptInfo.currentRunNum}")
                        } }
                    }
                }
                ActionString.SKIP ->{
                    scriptActionInfo.command.add { scriptActionInfo.skip ()}
                }
                action.startsWith(  ActionString.CHECK   ).toString() ->{
                    val setId = action.substring(   ActionString.CHECK.length, action.length-1   ).toInt()
                    scriptActionInfo.command.add{ scriptActionInfo.check(setId) }
                }
                action.startsWith(  ActionString.UN_FIND   ).toString()->{
                    println(action.substring(   ActionString.UN_FIND.length, action.length-1   )+"__SubString")
                    action.substring(   ActionString.UN_FIND.length, action.length-1   ).split(",").forEach{
                        scriptActionInfo.picNotFoundList.add(it)
                    }
                }
                action.startsWith(  ActionString.SLEEP  ).toString() -> {
                    val sleepTime = action.substring(   ActionString.SLEEP.length, action.length-1   ).toLong()
                    scriptActionInfo.command.add{ scriptActionInfo.sleep(sleepTime)}
                }
                action.startsWith(  ActionString.OVER_SET  ).toString() -> {
                    //val setId = action.substring(   ActionString.OVER_SET.length, action.length-1   ).toInt()
                    scriptActionInfo.command.add{ scriptActionInfo.overSet(scriptSetInfo) }
                }
                action.startsWith(  ActionString.OVER_SET_AND_PARENT  ).toString() -> {
                    //val setId = action.substring(   ActionString.OVER_SET_AND_PARENT.length, action.length-1   ).toInt()
                    scriptActionInfo.command.add{ scriptActionInfo.overSet(scriptSetInfo, alsoUpdateParent = true) }
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