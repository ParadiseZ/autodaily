package com.smart.autodaily.handler

import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.mutableLongStateOf
import com.smart.autodaily.command.AdbBack
import com.smart.autodaily.command.AdbClick
import com.smart.autodaily.command.AdbPartClick
import com.smart.autodaily.command.CAPTURE
import com.smart.autodaily.command.Return
import com.smart.autodaily.command.Skip
import com.smart.autodaily.command.Sleep
import com.smart.autodaily.command.adbRebootApp
import com.smart.autodaily.command.adbStartApp
import com.smart.autodaily.constant.ActionString
import com.smart.autodaily.constant.MODEL_BIN
import com.smart.autodaily.constant.MODEL_PARAM
import com.smart.autodaily.data.appDb
import com.smart.autodaily.data.entity.DetectResult
import com.smart.autodaily.data.entity.Point
import com.smart.autodaily.data.entity.Rect
import com.smart.autodaily.data.entity.ScriptActionInfo
import com.smart.autodaily.data.entity.ScriptInfo
import com.smart.autodaily.data.entity.ScriptRunStatus
import com.smart.autodaily.data.entity.ScriptSetInfo
import com.smart.autodaily.utils.ModelUtil
import com.smart.autodaily.utils.ScreenCaptureUtil
import com.smart.autodaily.utils.ShizukuUtil
import com.smart.autodaily.utils.debug
import com.smart.autodaily.utils.isBetweenHour
import com.smart.autodaily.utils.partScope
import com.smart.autodaily.utils.runScope
import com.smart.autodaily.utils.toastOnUi
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import splitties.init.appCtx


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
        _globalSetMap.value.takeIf {
            it.isEmpty()
        }?.let {
            appDb!!.scriptSetInfoDao.getGlobalSet().associateBy {
                it.setId
            }.let {
                this._globalSetMap.value = it
            }
        }
    }

    //shell运行
    suspend fun runScriptByAdb(){
        val intervalTime = _globalSetMap.value[3]?.setValue?.toFloat()?.times(1000)?.toLong()?:2000L
        val similarScore =  _globalSetMap.value[5]?.run { this.setValue?.toFloat() }?:0.5f
        val rebootTime =  _globalSetMap.value[4]?.run { this.setValue?.toFloat()?.times(60000)?.toLong() }?: 600000L
        val useGpu = _globalSetMap.value[10]?.setValue?.toBoolean()?:false
        //println("开始运行${_scriptCheckedList.value.size}")
        println("select num："+_scriptCheckedList.value.size)
        _scriptCheckedList.value.forEach scriptForEach@{ si->
            if(si.currentRunNum < si.runsMaxNum){
                //启动app
                try {
                    partScope.launch {
                        println("start APP")
                        adbStartApp(si.packageName)
                    }
                    println("load model")
                    //ModelUtil.reloadModel(si.modelPath+"/model.ncnn", si.imgSize, useGpu)
                    appCtx.getExternalFilesDir("")?.let {
                        //读取模型
                        ModelUtil.reloadModelSec(
                            it.path+"/"+si.modelPath+"/"+ MODEL_PARAM,
                            it.path+"/"+si.modelPath+"/"+ MODEL_BIN,
                            si.imgSize , useGpu
                        )
                    }
                }catch (e: Exception){
                    println("start app failed")
                    appCtx.toastOnUi("app启动失败！")
                    return
                }
                //最小子项
                val maxSet = appDb!!.scriptSetInfoDao.getScriptSetByScriptId(si.scriptId, 1)
                //最小子项的上级or项
                val orSet = appDb!!.scriptSetInfoDao.getScriptSetByScriptId(si.scriptId, 2)
                orSet.forEach{
                    if( appDb!!.scriptSetInfoDao.getChildCheckedCount(it.scriptId, it.flowParentId!!) == 0){
                        maxSet.add(it)
                    }
                }
                val scriptSet = maxSet.filter{
                    it.flowParentIdList = it.flowParentId!!.split(",").map { flowId -> flowId.toInt() }
                    //筛选选中的
                    it.flowParentIdList.size == appDb!!.scriptSetInfoDao.countCheckedNumByParentFlowId(it.scriptId, it.flowParentIdList)
                }.filter {  item ->
                    //筛选符合当前时间段的
                    when {
                        item.flowIdType == 4 -> true //  flowIdType 为 4 直接保留
                        isBetweenHour(6, 12) -> item.flowIdType == 1 // 时间在 6 点到 12 点之间
                        isBetweenHour(12, 18) -> item.flowIdType == 2 // 时间在 12 点到 18 点之间
                        else -> item.flowIdType == 3 // 其他时间
                    }
                }

                //保存的所有的action map
                val scriptActionMap : HashMap<Int,ScriptActionInfo> = hashMapOf()
                //“返回”类的flow_id
                val backSetFlowIds = appDb!!.scriptSetInfoDao.getBackSetByScriptId(si.scriptId)
                //遍历的返回操作合集
                val backActionArrayList = ArrayList<ScriptActionInfo>()
                //返回类的action，设置返回操作合集
                appDb!!.scriptActionInfoDao.getBackActionByScriptId(si.scriptId, backSetFlowIds).forEach {
                    //从所有action中获取，避免重复初始化操作
                    val res = scriptActionMap.getOrPut(it.id) {
                        initActionFun(it)
                        it.onlyLabels?.let { onlyLabels ->
                            it.onlyLabelSet =
                                onlyLabels.split(",").map { labelStr -> labelStr.toInt() }
                                    .toSet()
                        }
                        it.pageLabels?.let { pageLabels ->
                            it.pageLabelSet =
                                pageLabels.split(",").map { labelStr -> labelStr.toInt() }
                                    .toSet()
                        }
                        it.exceptLabels?.let { exceptLabels ->
                            it.exceptLabelSet =
                                exceptLabels.split(",").map { labelStr -> labelStr.toInt() }
                                    .toSet()
                        }
                        it
                    }
                    backActionArrayList.add(res)
                }
                //跳跃使用
                val jumpData = hashMapOf(false to -1)
                scriptSet.forEach setForEach@ { set->
                    val jumpFlowId = jumpData[true]
                    //跳跃或有今天的执行记录，则遍历下一条
                    if((jumpFlowId!=null && set.flowParentIdList.contains(jumpFlowId) )
                        || appDb!!.scriptRunStatusDao.countByFlowIdAndType(set.flowId!!, set.flowIdType, LocalDate.now().toString()) > 0 ){
                        return@setForEach
                    }
                    jumpData[false] = -1

                    //遍历的操作合集
                    val scriptActionArrayList = ArrayList<ScriptActionInfo>()
                    //包含全局设置的action，设置操作合集
                    appDb!!.scriptActionInfoDao.getCheckedBySetId( set.scriptId, set.flowParentIdList, set.flowIdType ).forEach {
                        //从所有action中获取，避免重复初始化操作
                        val res = scriptActionMap.getOrPut(it.id) {
                            initActionFun(it)
                            it.onlyLabels?.let { onlyLabels ->
                                it.onlyLabelSet =
                                    onlyLabels.split(",").map { labelStr -> labelStr.toInt() }
                                        .toSet()
                            }
                            it.pageLabels?.let { pageLabels ->
                                it.pageLabelSet =
                                    pageLabels.split(",").map { labelStr -> labelStr.toInt() }
                                        .toSet()
                            }
                            it.exceptLabels?.let { exceptLabels ->
                                it.exceptLabelSet =
                                    exceptLabels.split(",").map { labelStr -> labelStr.toInt() }
                                        .toSet()
                            }
                            it
                        }
                        scriptActionArrayList.add(res)
                    }
                    val remRebootTime = mutableLongStateOf(System.currentTimeMillis())
                    run LoopDo@{
                        when(loopDo(scriptActionArrayList, set, si.classesNum, jumpData , intervalTime,rebootTime, similarScore,si.packageName,remRebootTime)){
                            "setForEach"->return@setForEach
                            "capException" -> println("capException")
                            //无匹配标签，尝试在返回操作集中寻找
                            "NO_MATCH_LABELS" ->  {
                                println("使用返回列表")
                                loopDo(backActionArrayList, set, si.classesNum, jumpData , intervalTime,rebootTime, similarScore,si.packageName,remRebootTime)
                                return@LoopDo
                            }
                            else->{
                                println("LoopDo ELSE")
                            }
                        }
                    }
                }//set for each
            }else{
                si.currentRunNum = 0
                si.nextRunDate = LocalDate.now().toString()
                println(si.nextRunDate)
                //appDb!!.scriptInfoDao.update(si)
            }
        }
    }

    private suspend fun loopDo(
        actionList : List<ScriptActionInfo>,
        set : ScriptSetInfo,
        classNum : Int,
        jumpData : HashMap<Boolean, Int>,
        intervalTime : Long,
        rebootTime : Long,
        similarScore : Float,
        packName : String,
        remRebootTime: MutableLongState) : String{
        var detectRem : Array<DetectResult> = emptyArray()
        //重试次数
        var curentRetry = 0
        //重试延迟
        val retryDelay = 10000L
        //当前脚本已选操作
        while ( true ) {
            run capLoop@{
                delay(intervalTime)
                when (val captureBitmap = ShizukuUtil.iUserService?.execCap(CAPTURE)) {
                    null -> {
                        debug("captureBitmap is null")
                        return "capException"
                    }

                    else -> {
                        val detectResArray = ModelUtil.model.detect(captureBitmap, classNum)
                        when {
                            detectResArray.isEmpty() -> {
                                isTimeOut(remRebootTime.longValue, rebootTime).takeIf { it }?.let { partScope.launch { adbRebootApp(packName) } }
                            }
                            else -> {
                                when {
                                    detectRem.size == detectResArray.size && detectRem.contentDeepEquals(detectResArray
                                    ) -> {
                                        println("与上次识别结果一致，等待响应")
                                        isTimeOut(remRebootTime.longValue, rebootTime).takeIf { it }
                                            ?.let {
                                                partScope.launch {
                                                    adbRebootApp(packName)
                                                }
                                            }
                                        if (curentRetry < 2) {
                                            curentRetry += 1
                                            delay(retryDelay)
                                            detectRem = emptyArray()
                                        }
                                        return@capLoop
                                    }

                                    else -> {
                                        detectRem = detectResArray.copyOf()
                                    }
                                }
                                val detectRes =
                                    detectResArray.toList().filter { it.prob > similarScore }
                                        .toTypedArray()
                                val detectLabels =
                                    detectRes.map { it.label }.toSet().sortedBy { it }
                                remRebootTime.longValue = System.currentTimeMillis()
                                debugPrintScriptActionLabels(detectRes, detectLabels)
                                actionList.forEach scriptAction@{ sai ->
                                    if (sai.skipFlag) {
                                        return@scriptAction
                                    }
                                    if (
                                        (sai.onlyLabelSet.isNotEmpty() && detectLabels.size == sai.onlyLabelSet.size && detectLabels.containsAll(
                                            sai.onlyLabelSet
                                        )) ||
                                        (sai.pageLabelSet.isNotEmpty() && detectLabels.containsAll(
                                            sai.pageLabelSet
                                        ) && sai.exceptLabelSet.none { detectLabels.contains(it) })
                                    ) {

                                        sai.command.onEach cmdForEach@{ cmd ->
                                            when (cmd) {
                                                is Return -> {
                                                    when (cmd.type) {
                                                        ActionString.FINISH -> {
                                                            if (set.backFlag ==0 && appDb!!.scriptRunStatusDao.countByFlowIdAndType(
                                                                    sai.flowId,
                                                                    set.flowIdType,
                                                                    dateTime = LocalDate.now()
                                                                        .toString()
                                                                ) == 0
                                                            ) {
                                                                val scriptStatus = ScriptRunStatus(
                                                                    flowId = sai.flowId,
                                                                    flowIdType = set.flowIdType,
                                                                    curStatus = 2,
                                                                    dateTime = LocalDate.now()
                                                                        .toString()
                                                                )
                                                                appDb!!.scriptRunStatusDao.insert(
                                                                    scriptStatus
                                                                )
                                                                println("非返回类，插入数据$scriptStatus")
                                                            }
                                                            println("结束：$sai.pageDesc")
                                                            return "setForEach"
                                                        }

                                                        ActionString.JUMP -> {
                                                            jumpData[true] = sai.flowId
                                                            println("跳跃：$sai.pageDesc")
                                                            return "setForEach"
                                                        }
                                                    }
                                                }

                                                is AdbPartClick -> {
                                                    setClickPartPoints(sai, cmd, detectRes)
                                                    if (sai.executeMax > 1) {
                                                        sai.executeCur += 1
                                                        if (sai.executeCur >= sai.executeMax) {
                                                            sai.executeCur = 0
                                                            sai.skipFlag = true
                                                        }
                                                    }
                                                    cmd.exec(sai)
                                                }

                                                is AdbClick -> {
                                                    setPoints(
                                                        sai,
                                                        detectRes,
                                                        detectLabels.size < detectRes.size
                                                    )
                                                    if (sai.executeMax > 1) {
                                                        sai.executeCur += 1
                                                        if (sai.executeCur >= sai.executeMax) {
                                                            sai.executeCur = 0
                                                            sai.skipFlag = true
                                                        }
                                                    }
                                                    cmd.exec(sai)
                                                }

                                                else -> {
                                                    cmd.exec(sai)
                                                }
                                            }

                                        }
                                        println("找到${sai.pageDesc} || ${sai.flowId} || ${set.flowParentId}")
                                        return@capLoop
                                    }
                                }
                                //有标签内容但未识别到
                                return "NO_MATCH_LABELS"
                            }
                        }
                    }
                }
            } //capLoop
        }//WHILE LOOP
    }

    private fun isTimeOut(remRebootTime : Long,rebootTime: Long) : Boolean{
        if (System.currentTimeMillis() - remRebootTime > rebootTime){
            return true
        }
        return false
    }

    private fun setPoints(sai: ScriptActionInfo, detectRes: Array<DetectResult>, multiple: Boolean){
        if (sai.clickLabelPosition == 5){
            return
        }
        if (multiple && sai.clickLabelPosition !=-1){
            val displayMetrics = ScreenCaptureUtil.getDisplayMetrics()
            val xDisCenter = displayMetrics.widthPixels/2
            val yDisCenter = displayMetrics.heightPixels/2
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
            val clickRes = detectRes.firstOrNull {
                it.label == sai.clickLabelIdx
            }
            globalSetMap.value[9]?.setValue?.let {
                if (clickRes != null){
                    sai.point = Point(clickRes.xCenter + it.toFloat(), clickRes.yCenter + it.toFloat())
                }
            } }
    }

    private fun setClickPartPoints(sai: ScriptActionInfo,adbPart : AdbPartClick, detectRes: Array<DetectResult>){
        val clickRes = detectRes.firstOrNull { it.label == sai.clickLabelIdx }
        globalSetMap.value[9]?.setValue?.let {
            if (clickRes != null){
                if (adbPart.type=="x"){
                    sai.point = Point(clickRes.rect.x+clickRes.rect.width/adbPart.part/2 * (2*adbPart.idx -1)+ it.toFloat(), clickRes.yCenter + it.toFloat())
                }else{
                    sai.point = Point(clickRes.xCenter + it.toFloat(), clickRes.rect.y+clickRes.rect.height/adbPart.part/2 * (2*adbPart.idx -1)+ it.toFloat())
                }
            }
        }
    }
    //操作映射为函数
    private fun initActionFun(scriptActionInfo: ScriptActionInfo){
        try {
            scriptActionInfo.actionString.split(";").forEach {  action->
                //debug("action = ${action}")
                when(action){
                    ActionString.CLICK-> {
                        if (scriptActionInfo.clickLabelPosition == 5){
                            val displayMetrics = ScreenCaptureUtil.getDisplayMetrics(appCtx)
                            val point =Point((displayMetrics.widthPixels/2).toFloat(),(displayMetrics.heightPixels/2).toFloat())
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
                    ActionString.JUMP ->{
                        scriptActionInfo.command.add(Return(ActionString.JUMP))
                    }
                    ActionString.BACK ->{
                        scriptActionInfo.command.add(AdbBack())
                    }
                    else ->{
                        when{
                            action.startsWith(  ActionString.SLEEP  ) -> {
                                val sleepTime = action.substring(   ActionString.SLEEP.length+1, action.length-1   ).toLong()
                                scriptActionInfo.command.add(Sleep(sleepTime))
                            }
                            action.startsWith( ActionString.CLICK_PART ) ->{
                                val type = if(action.contains("x")){"x"}else{"y"}
                                val lastIdx =  action.indexOfLast { it==',' }
                                val part = action.substring(   ActionString.CLICK_PART.length+3, lastIdx ).toInt()
                                val idx = action.substring(action.indexOfLast { it==',' }+1, action.length-1 ).toInt()
                                scriptActionInfo.command.add(AdbPartClick(type, part, idx))
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
        }catch (e : Exception){
            println(scriptActionInfo)
            println(e.message)
            runScope.coroutineContext.cancelChildren()
            appCtx.toastOnUi("初始化action失败，请联系管理员！")
        }
    }

    /*private fun runningExceptionHandler(toastMsg : String){
        runScope.coroutineContext.cancelChildren()
        appCtx.toastOnUi(toastMsg)
    }*/

    //匹配
    //初始化已选择脚本数据，HomeScreen调用
    fun initScriptData(scriptList : List<ScriptInfo>){
        this._scriptCheckedList.value = scriptList
    }

    private fun debugPrintScriptActionLabels(detectRes : Array<DetectResult>?=null,detectLabels: List<Int>?=null,sai: ScriptActionInfo?=null){
        detectRes?.let {
            println("detectRes.size："+detectRes.size)
            detectRes.forEach { res->
                println(res)
            }
        }
        detectLabels?.let {
            println("detectLabels.size："+detectLabels.size)
            detectLabels.forEach { res->
                println(res)
            }
        }
        sai?.let { sai2->
            println("sai.onlyLabelSet.size："+sai2.onlyLabelSet.size)
            sai2.onlyLabelSet.forEach {
                println(it)
            }
            println("sai.pageLabelSet.size："+sai2.pageLabelSet.size)
            sai2.pageLabelSet.forEach {
                println(it)
            }
            println("sai.exceptLabelSet.size："+sai2.exceptLabelSet.size)
            sai2.exceptLabelSet.forEach{
                println(it)
            }
        }
        sai?.let {sai2->
            detectRes?.let {
                detectLabels?.let {
                    println(detectLabels.size == sai2.onlyLabelSet.size && detectLabels.containsAll(sai2.onlyLabelSet))
                    println(sai2.pageLabelSet.let { detectLabels.containsAll(it) })
                    println( sai2.exceptLabelSet.none { detectLabels.contains(it) })
                }
            }
        }
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