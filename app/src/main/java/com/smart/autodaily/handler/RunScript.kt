package com.smart.autodaily.handler

import com.smart.autodaily.command.AdbClick
import com.smart.autodaily.command.AdbSumClick
import com.smart.autodaily.command.CAPTURE
import com.smart.autodaily.command.Check
import com.smart.autodaily.command.Return
import com.smart.autodaily.command.START
import com.smart.autodaily.command.Skip
import com.smart.autodaily.command.Sleep
import com.smart.autodaily.constant.ActionString
import com.smart.autodaily.constant.WORK_TYPE01
import com.smart.autodaily.constant.WORK_TYPE02
import com.smart.autodaily.constant.WORK_TYPE03
import com.smart.autodaily.data.appDb
import com.smart.autodaily.data.entity.DetectResult
import com.smart.autodaily.data.entity.Point
import com.smart.autodaily.data.entity.Rect
import com.smart.autodaily.data.entity.ScriptActionInfo
import com.smart.autodaily.data.entity.ScriptInfo
import com.smart.autodaily.data.entity.ScriptSetInfo
import com.smart.autodaily.utils.ModelUtil
import com.smart.autodaily.utils.ScreenCaptureUtil
import com.smart.autodaily.utils.ShizukuUtil
import com.smart.autodaily.utils.debug
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    fun initGlobalSet(){
        //map
        appDb!!.scriptSetInfoDao.getGlobalSet().associateBy {
            it.setId
        }.let {
            this._globalSetMap.value = it
        }
    }

    suspend fun runScript(scriptSetInfo: ScriptSetInfo) {
        when(scriptSetInfo.setValue){
            WORK_TYPE01 ->{}
            WORK_TYPE02 -> {
                //runScriptByAdb()
                println("runScriptByAdb()")
                delay(5000)
            }
            WORK_TYPE03 -> {}
        }
    }

    //shell运行
    private suspend fun runScriptByAdb(){
        //println("开始运行${_scriptCheckedList.value.size}")
        _scriptCheckedList.value.forEach scriptForEach@{ si->
            if(si.currentRunNum < si.runsMaxNum){
                //启动app
                try {
                    ShizukuUtil.iUserService?.execLine(START+si.packageName)
                    //读取模型
                    ScreenCaptureUtil.getDisplayMetrics(appCtx)
                        ?.let { ModelUtil.reloadModel("bh3/cn/model.ncnn", 640,false) }
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
                        it.labelSet =  it.pageLabels.split(",").map{   labelStr->
                            labelStr.toInt()
                        }.toSet()
                        it.exceptLabels?.let { exceptLabels->
                            it.exceptLabelSet = exceptLabels.split(",").map{   labelStr->
                                labelStr.toInt()
                            }.toSet()
                        }
                        it.needFindSize = it.labelSet.size
                    }
                    println("set:${set}")
                    println("setId:${checkIsFinish(set.setId)}")
                    while ( !checkIsFinish(set.setId) ) {                    //当前脚本已选操作
                        val capTime = System.currentTimeMillis()
                        val captureBitmap= ShizukuUtil.iUserService?.execCap(CAPTURE)
                        //val drawResult = captureBitmap?.copy(Bitmap.Config.ARGB_8888, true)

                        if (captureBitmap!=null) {
                            val detectRes = ModelUtil.model.detect(captureBitmap,3)
                            /*val detectRes = ModelUtil.model.detectAndDraw(captureBitmap,3,drawMap = drawResult)
                            println("captureBitmap:${captureBitmap.width},detectRes:${detectRes.size}")
                            for (i in detectRes.indices){
                                detectRectFList.add(detectRes[i].rect)
                                println("lab："+detectRes[i].label + ",prob："+detectRes[i].prob + ",rect："+detectRes[i].rect + ",detect："+detectRes[i])
                            }*/
                            if (detectRes.isNotEmpty()){
                                val detectLabels = detectRes.map { it.label }.toSet().sortedBy {
                                    it
                                }
                                scriptActionList.forEach scriptAction@{ sai->
                                    if (sai.labelSet.intersect(detectLabels).size == sai.needFindSize){
                                        //找到执行
                                        sai.command.onEach { cmd->
                                            if(cmd is Return){
                                                when(cmd.type){
                                                    ActionString.OVER_SET->{
                                                        appDb!!.scriptSetInfoDao.updateResultFlag(sai.setId , true)
                                                    }
                                                    ActionString.FINISH ->{
                                                        if (si.currentRunNum < si.runsMaxNum) {
                                                            si.currentRunNum += 1
                                                            appDb!!.scriptInfoDao.update(si)
                                                        }
                                                        return@scriptForEach
                                                    }
                                                }
                                            }else if(cmd is AdbClick){
                                                setPoints(sai, detectRes, detectRes.size > sai.needFindSize)
                                                cmd.exec(sai)
                                            }else{
                                                if (    !cmd.exec(sai)   ){
                                                    return@scriptAction
                                                }
                                            }
                                        }
                                    }else{
                                        if (System.currentTimeMillis() - capTime < 3000) {
                                            delay(3000)
                                        }
                                    }
                                }
                            }
                        }else{
                            debug("captureBitmap is null")
                            return
                        }
                    } //while for each
                }//set for each
            }else{
                si.currentRunNum = 0
                si.nextRunDate = Date().toString()
                println(si.nextRunDate)
                //appDb!!.scriptInfoDao.update(si)
            }
        }
    }

    private fun setPoints(sai: ScriptActionInfo, detectRes: Array<DetectResult>, multiple: Boolean){
        if (sai.clickLabelPosition == 5){
            return
        }
        if (multiple && sai.clickLabelPosition !=-1){
            val xDisCenter = ScreenCaptureUtil.displayMetrics!!.widthPixels/2
            val yDisCenter = ScreenCaptureUtil.displayMetrics!!.heightPixels/2
            var condition : (Rect, Float, Float) -> Boolean ={ _: Rect, _: Float, _: Float -> false}
            when(sai.clickLabelPosition){
                1 ->{
                    condition = {
                        rect: Rect, x:Float , y:Float ->  rect.x < x && rect.y < y
                    }
                }
                2->{
                    condition = {
                        rect: Rect, x:Float , y:Float -> rect.x > x && rect.y < y
                    }
                }
                3->{
                    condition = {
                        rect: Rect, x:Float , y:Float -> rect.x < x && rect.y > y
                    }
                }
                4->{
                    condition = {
                        rect: Rect, x:Float , y:Float -> rect.x > x && rect.y > y
                    }
                }
            }
            detectRes.filter {
                    it.label == sai.clickLabelIdx &&  condition(it.rect,xDisCenter.toFloat(),yDisCenter.toFloat())
                }.map  {
                    sai.point = Point(it.xCenter, it.yCenter)
                }
        }else{
            val clickRes = detectRes.firstOrNull { it.label == sai.clickLabelIdx }
            if (clickRes != null) {
                sai.point = Point(clickRes.xCenter, clickRes.yCenter)
            }
        }
    }

    //操作映射为函数
    private fun initActionFun(scriptActionInfo: ScriptActionInfo, scriptInfo: ScriptInfo, scriptSetInfo: ScriptSetInfo){
        scriptActionInfo.actionString.split(";").forEach {  action->
            //debug("action = ${action}")
            when(action){
                ActionString.CLICK-> {
                    if (scriptActionInfo.clickLabelPosition == 5){
                        val point =Point((ScreenCaptureUtil.displayMetrics!!.widthPixels/2).toFloat(),(ScreenCaptureUtil.displayMetrics!!.heightPixels/2).toFloat())
                        scriptActionInfo.command.add (AdbClick(point) )
                    }else{
                        scriptActionInfo.command.add (AdbClick())
                    }
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
}