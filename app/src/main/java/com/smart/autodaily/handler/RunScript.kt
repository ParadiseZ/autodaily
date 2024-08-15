package com.smart.autodaily.handler

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.smart.autodaily.command.AdbClick
import com.smart.autodaily.command.AdbSumClick
import com.smart.autodaily.command.CAPTURE
import com.smart.autodaily.command.Check
import com.smart.autodaily.command.Return
import com.smart.autodaily.command.START
import com.smart.autodaily.command.Skip
import com.smart.autodaily.command.Sleep
import com.smart.autodaily.constant.ActionString
import com.smart.autodaily.data.appDb
import com.smart.autodaily.data.entity.Point
import com.smart.autodaily.data.entity.ScriptActionInfo
import com.smart.autodaily.data.entity.ScriptInfo
import com.smart.autodaily.data.entity.ScriptSetInfo
import com.smart.autodaily.data.entity.WORK_TYPE01
import com.smart.autodaily.data.entity.WORK_TYPE02
import com.smart.autodaily.data.entity.WORK_TYPE03
import com.smart.autodaily.ui.conponent.detectRectFList
import com.smart.autodaily.ui.conponent.imgLayout
import com.smart.autodaily.utils.ModelUtil
import com.smart.autodaily.utils.ScreenCaptureUtil
import com.smart.autodaily.utils.ShizukuUtil
import com.smart.autodaily.utils.debug
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import splitties.init.appCtx
import java.util.Date


object  RunScript {

    private val _scriptCheckedList = MutableStateFlow<List<ScriptInfo>>(emptyList())

    private val _globalSetMap = MutableStateFlow<Map<Int, ScriptSetInfo>>(emptyMap())
    val globalSetMap : StateFlow<Map<Int, ScriptSetInfo>> get() = _globalSetMap

    //val scriptCheckedList : StateFlow<List<ScriptInfo>> = _scriptCheckedList
    //var scriptSetList : List<ScriptInfo> = emptyList()
    //val globalSetList =  MutableStateFlow<List<ScriptSetInfo>>(emptyList())
    //var scriptRunCoroutineScope = CoroutineScope(Dispatchers.IO)
    var scriptRunCoroutineScope = CoroutineScope(Dispatchers.IO)

    fun initGlobalSet(){
        appDb!!.scriptSetInfoDao.getGlobalSet().associateBy {
            it.setId
        }.let {
            this._globalSetMap.value = it
        }
    }

    fun runScript(scriptSetInfo: ScriptSetInfo, alterResult : ()->Unit) {
        //initOrb()
        //已选脚本
        scriptRunCoroutineScope.launch {
            when(scriptSetInfo.setValue){
                WORK_TYPE01 ->{}
                WORK_TYPE02 -> {
                    runScriptByAdb(alterResult)
                }
                WORK_TYPE03 -> {}
            }
            /*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                runScriptByAdb()
            }*/
        }

    }

    //shell运行
    private suspend fun runScriptByAdb(alterResult:()->Unit){
        //println("开始运行${_scriptCheckedList.value.size}")
        _scriptCheckedList.value.forEach scriptForEach@{ si->
            if(si.currentRunNum < si.runsMaxNum){
                //启动app
                try {
                    ShizukuUtil.iUserService?.execLine(START+si.packageName)
                    //读取模型
                    ScreenCaptureUtil.getDisplayMetrics(appCtx)
                        ?.let { ModelUtil.reloadModel("bh3/cn/model.ncnn", 1280,false) }
                }catch (e: Exception){
                    println("app启动失败！")
                    //appCtx.toastOnUi("app启动失败！")
                    //return@scriptForEach
                }
                //获取屏幕宽高
                ScreenCaptureUtil.setDisplayMetrics(appCtx)
                //println("DisplayMetrics:${ScreenCaptureUtil.displayMetrics?.heightPixels}}")
                val scriptSetInfo = appDb!!.scriptSetInfoDao.getScriptSetByScriptIdLv0(si.scriptId)
                scriptSetInfo.forEach setForEach@ { set->
                    val setIds = appDb!!.scriptSetInfoDao.getScriptSetParentAndChild( set.setId )
                    println("setIds = $setIds")
                    val scriptActionList = appDb!!.scriptActionInfoDao.getCheckedBySetId( setIds, si.scriptId )
                    scriptActionList.forEach {
                        initActionFun(it, si, set)
                        it.picNeedFindList = it.picNameList.split(",")
                        it.picNotFoundList?.let { pic->
                            it.picNotNeedFindList = pic.split(",")
                        }
                        it.stepString?.let {step->
                            it.stepList = step.split(",").map {setId->
                                setId.toInt()
                            }
                        }
                    }
                    println("set:${set}")
                    println("setId:${checkIsFinish(set.setId)}")
                    while ( !checkIsFinish(set.setId) ) {                    //当前脚本已选操作

                        //while (si.currentRunNum < si.runsMaxNum) {

                        val capTime = System.currentTimeMillis()
                        val captureBitmap= ShizukuUtil.iUserService?.execCap(CAPTURE)
                        val drawResult = captureBitmap?.copy(Bitmap.Config.ARGB_8888, true)

                        if (captureBitmap!=null && drawResult!=null) {
                            val detectRes = ModelUtil.model.detectAndDraw(captureBitmap,3,drawMap = drawResult)
                            //val detectRes = ModelUtil.model.detect(captureBitmap,3)
                            println("captureBitmap:${captureBitmap.width},detectRes:${detectRes.size}")
                            delay(5000)
                            imgLayout.value = captureBitmap.asImageBitmap()
                            for (i in 0..5){
                                detectRectFList.add(detectRes[i].rect)
                                println("lab："+detectRes[i].label + ",prob："+detectRes[i].prob + ",rect："+detectRes[i].rect + ",detect："+detectRes[i])
                            }
                            delay(5000)
                            imgLayout.value = drawResult.asImageBitmap()
                            delay(10000)
                            return
                            /*val bitmap : Bitmap  = Bitmap.createBitmap(1280, 720, Bitmap.Config.ARGB_8888 )
                            Utils.matToBitmap(sourceMat, bitmap)
                            ScreenCaptureUtil.saveScreenCapture(bitmap)*/
                            // Check and convert image depth if necessary
                            scriptActionList.forEach scriptAction@{
                                if (!it.skipFlag) {
                                    //寻找，目的为找到，所有的都找到则继续
                                    //println("picNeedFindList:${it.picNeedFindList}")
                                    //if(templateMatch(si.picPath, it.picNeedFindList, sourceMat, _globalSetMap.value[5]!!.setValue!!.toDouble(), true,it,true)){
                                    println("point：${it.point}")
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
                                                ActionString.UN_FIND ->{
                                                    //寻找，目的为未找到，所有的都未找到则继续
                                                    //if(!templateMatch(si.picPath, it.picNotNeedFindList, sourceMat, _globalSetMap.value[5]!!.setValue!!.toDouble(), true,it,false)){
                                                    return@scriptAction
                                                    //}
                                                }
                                            }
                                        } else{
                                            if (    !cmd.exec(it)   ){
                                                return@scriptAction
                                            }
                                        }
                                    }
                                    //}

                                }
                            }
                            if (System.currentTimeMillis() - capTime < 3000) {
                                delay(3000)
                            }
                        }else{
                            debug("captureBitmap is null")
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
        scriptActionInfo.actionString.split(";").forEach {  action->
            //debug("action = ${action}")
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
                ActionString.UN_FIND ->{
                    scriptActionInfo.command.add(Return(ActionString.UN_FIND))
                }
                else ->{
                    when{
                        (action.startsWith(  ActionString.CLICK  ) && action.length > 8)  -> {
                            val (x,y) =  action.substring(   ActionString.CLICK.length+1, action.length-1  ).split(",")
                            val point = Point(x.toFloat(),y.toFloat())
                            scriptActionInfo.command.add(AdbSumClick(point))
                        }
                        action.startsWith(  ActionString.SLEEP  ) -> {
                            val sleepTime = action.substring(   ActionString.SLEEP.length+1, action.length-1   ).toLong()
                            scriptActionInfo.command.add(Sleep(sleepTime))
                        }
                        action.startsWith(  ActionString.CHECK   ) ->{
                            val setId = action.substring(   ActionString.CHECK.length+1, action.length-1   ).toInt()
                            scriptActionInfo.command.add( Check(setId) )
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

    //匹配

    //检测子设置是否完成
    private fun checkIsFinish(setId : Int) : Boolean{
        return appDb!!.scriptSetInfoDao.getChildResultFlag(setId)
    }

    //初始化已选择脚本数据，HomeScreen调用
    fun initScriptData(scriptList : List<ScriptInfo>){
        this._scriptCheckedList.value = scriptList
    }
    fun stopRunScript(){
        scriptRunCoroutineScope.cancel()
    }

/*    fun updateScript(scriptInfo: ScriptInfo){
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
    }*/

    private fun checkAndRestartScopeState(){
        if (
            !scriptRunCoroutineScope.isActive
        ){
            scriptRunCoroutineScope = CoroutineScope(Dispatchers.IO)
        }
    }

}