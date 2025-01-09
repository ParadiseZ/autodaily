package com.smart.autodaily.handler

import android.graphics.Bitmap
import androidx.collection.mutableIntSetOf
import androidx.compose.runtime.mutableIntStateOf
import com.smart.autodaily.command.AdbBack
import com.smart.autodaily.command.AdbClick
import com.smart.autodaily.command.AdbPartClick
import com.smart.autodaily.command.CAPTURE
import com.smart.autodaily.command.Operation
import com.smart.autodaily.command.Return
import com.smart.autodaily.command.Skip
import com.smart.autodaily.command.SkipFlowId
import com.smart.autodaily.command.Sleep
import com.smart.autodaily.command.adbRebootApp
import com.smart.autodaily.command.adbStartApp
import com.smart.autodaily.constant.ActionString
import com.smart.autodaily.constant.MODEL_BIN
import com.smart.autodaily.constant.MODEL_PARAM
import com.smart.autodaily.data.appDb
import com.smart.autodaily.data.entity.ConfigData
import com.smart.autodaily.data.entity.DetectResult
import com.smart.autodaily.data.entity.OcrResult
import com.smart.autodaily.data.entity.Point
import com.smart.autodaily.data.entity.Rect
import com.smart.autodaily.data.entity.ScriptActionInfo
import com.smart.autodaily.data.entity.ScriptInfo
import com.smart.autodaily.data.entity.ScriptRunStatus
import com.smart.autodaily.data.entity.ScriptSetInfo
import com.smart.autodaily.utils.AssetUtil
import com.smart.autodaily.utils.ModelUtil
import com.smart.autodaily.utils.ScreenCaptureUtil
import com.smart.autodaily.utils.ShizukuUtil
import com.smart.autodaily.utils.getMd5Hash
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

lateinit var conf : ConfigData
lateinit var set : ScriptSetInfo

val isRunning  by lazy {
    mutableIntStateOf(0)
}

val skipFlowIds  by lazy {
    mutableIntSetOf()
}

object  RunScript {

    private val _scriptCheckedList = MutableStateFlow<List<ScriptInfo>>(emptyList())

    private val _globalSetMap = MutableStateFlow<Map<Int, ScriptSetInfo>>(emptyMap())
    val globalSetMap : StateFlow<Map<Int, ScriptSetInfo>> get() = _globalSetMap


    //val scriptCheckedList : StateFlow<List<ScriptInfo>> = _scriptCheckedList
    //var scriptSetList : List<ScriptInfo> = emptyList()
    //val globalSetList =  MutableStateFlow<List<ScriptSetInfo>>(emptyList())
    //var scriptRunCoroutineScope = CoroutineScope(Dispatchers.IO)

    fun initGlobalSet(){
        appDb.scriptSetInfoDao.getGlobalSet().associateBy {
            it.setId
        }.let {
            this._globalSetMap.value = it
        }
    }

    private fun initConfData(){
        conf = ConfigData(
            _globalSetMap.value[3]?.setValue?.toFloat()?.times(1000)?.toLong()?:2000L,
            _globalSetMap.value[5]?.run { this.setValue?.toFloat() }?:0.5f,
            _globalSetMap.value[4]?.run { this.setValue?.toFloat()?.times(60000)?.toLong() }?: 600000L,
            _globalSetMap.value[10]?.checkedFlag?:false,
            _globalSetMap.value[1]?.checkedFlag?:false,
            globalSetMap.value[9]?.setValue?.toFloat()?:0f,
            _globalSetMap.value[6]?.setValue?.toInt()?:640,
            remRebootTime = System.currentTimeMillis(),
            10000L,
            2,
        )
    }

    fun testOcr(){
        val pic = AssetUtil.getFromAssets("0255.png")
        println("start ocr")
        ModelUtil.loadOcr(0,false, conf.detectSize)
        println("start detect")
        ModelUtil.model.detectOcr(pic)
    }
    //shell运行
    suspend fun runScriptByAdb(){
        initConfData()
        conf.toString()
        //println("开始运行${_scriptCheckedList.value.size}")
        println("select num："+_scriptCheckedList.value.size)
        _scriptCheckedList.value.forEach scriptForEach@{ si->
            skipFlowIds.clear()
            conf.pkgName = si.packageName
            //保存的所有的action map
            val allActionMap : HashMap<Int,ScriptActionInfo> = hashMapOf()
            if(si.currentRunNum < si.runsMaxNum){
                startApp( conf.pkgName )
                loadModel(si)
                println("load model,use gpu:${conf.useGpu}")
                println("初始化任务")
                //insertFirstDetect(si.classesNum, si.packageName)
                //所有选择的set
                val scriptSet = getScriptSets(si.scriptId)
                /*println("scriptSet")
                scriptSet.forEach {
                    println(it)
                }*/
                scriptSet.forEach setForEach@ { forSet->
                    set = forSet
                    println("当前任务：${set.setName},flow_id：${set.flowId}")
                    //跳跃或有今天的执行记录，则遍历下一条
                    if(appDb.scriptRunStatusDao.countByFlowIdAndType(set.scriptId,set.flowId!!, set.flowIdType, LocalDate.now().toString()) > 0 ){
                        return@setForEach
                    }
                    //遍历的操作合集
                    val scriptAction = appDb.scriptActionInfoDao.getCheckedBySetId( set.scriptId, set.flowParentIdList, set.flowIdType )
                    val scriptActionArrayList = actionsInit(scriptAction,allActionMap)
                    //遍历的返回操作合集
                    val backActionArrayList : ArrayList<ScriptActionInfo> = arrayListOf()
                    while (true){
                        //超时重启
                        isToReboot(si.packageName)
                        //截图延迟
                        val capture = if (conf.capture == null || conf.capture!!.isRecycled) {
                            delay(conf.intervalTime)
                            getPicture()?:continue
                        }else conf.capture!!
                        //MD5计算
                        conf.beforeHash = getMd5Hash(capture)
                        val detectRes = ModelUtil.model.detectYolo(capture, si.classesNum).toList().filter { it.prob > conf.similarScore }
                            .toTypedArray()
                        val detectLabels = detectRes.map { it.label }.toSet()

                        //OCR
                        val ocrRes =ModelUtil.model.detectOcr(capture)
                        //释放截图
                        conf.capture?.recycle()
                        println("回收后的 conf.capture${ conf.capture}")
                        val txtLabels = ocrRes.flatMap { it.label.toList() }.toSet()
                        //debugPrintScriptActionLabels(detectRes, detectLabels)
                        if (detectLabels.isEmpty() && txtLabels.isEmpty()) {
                            println("未识别到内容")
                            continue
                        }
                        //conf.curHash = getMd5Hash(capture)
                        when(tryAction(scriptActionArrayList, detectLabels, detectRes, txtLabels, ocrRes)){
                            1 ->{
                                //finish、jump类
                                println("setForEach")
                                return@setForEach
                            }
                            2 ->{
                                //整个操作执行结束
                            }
                            3 ->{
                                println("--------------------------------------")
                                if (conf.tryBackAction){
                                    println("尝试返回")
                                    tryBackAction( backActionArrayList, detectLabels,detectRes, txtLabels,ocrRes)
                                }
                            }
                            4 ->{
                                //操作无效/找到未操作（和操作前对比界面无变化）
                            }
                            5->{
                                //点无效
                            }
                        }
                    }
                }//set for each
                return@scriptForEach
            }else{
                si.currentRunNum = 0
                si.nextRunDate = LocalDate.now().toString()
                println(si.nextRunDate)
                //appDb.scriptInfoDao.update(si)
            }
        }
    }
    //启动APP
    private fun startApp(pkgName : String){
        try {
            partScope.launch {
                println("start APP")
                adbStartApp(pkgName)
            }
        }catch (e : Exception){
            println("start app failed")
            appCtx.toastOnUi( "app启动失败！")
            return
        }
    }

    private fun loadModel(si  : ScriptInfo){
        appCtx.getExternalFilesDir("")?.let {
            //读取模型
            ModelUtil.reloadModelSec(
                it.path+"/"+si.modelPath+"/"+ MODEL_PARAM,
                it.path+"/"+si.modelPath+"/"+ MODEL_BIN,
                si.imgSize , conf.useGpu
            )
        }
        ModelUtil.loadOcr(si.lang,conf.useGpu, conf.detectSize)
    }

    fun getPicture() : Bitmap?{
        return ShizukuUtil.iUserService?.execCap(CAPTURE)
    }

    private fun getScriptSets(scriptId : Int) : List<ScriptSetInfo>{
        //val curFlowId = appDb.scriptActionInfoDao.getCurFlowIdById(appDb.labelFtsDao.getMaxIdFromCurrent())
        val curFlowId = 0
        println("curFlowId:${curFlowId}")
        //最小子项
        val maxSet = appDb.scriptSetInfoDao.getScriptSetByScriptId(scriptId, curFlowId, 1)
        //最小子项的上级or项
        val orSet = appDb.scriptSetInfoDao.getScriptSetByScriptId(scriptId, curFlowId, 2)
        println("orSet")
        orSet.forEach{
            println(it)
            if( appDb.scriptSetInfoDao.getChildCheckedCount(it.scriptId, curFlowId, it.flowParentId!!) == 0){
                maxSet.add(it)
            }
        }
        return maxSet.filter{
            it.flowParentIdList = it.flowParentId!!.split(",").map { flowId -> flowId.toInt() }
            //筛选选中的
            it.flowParentIdList.size == appDb.scriptSetInfoDao.countCheckedNumByParentFlowId(it.scriptId, it.flowParentIdList)
        }.filter {  item ->
            //筛选符合当前时间段的
            when {
                item.flowIdType == 4 -> true //  flowIdType 为 4 直接保留
                isBetweenHour(6, 12) -> item.flowIdType == 1 // 时间在 6 点到 12 点之间
                isBetweenHour(12, 18) -> item.flowIdType == 2 // 时间在 12 点到 18 点之间
                else -> item.flowIdType == 3 // 其他时间
            }
        }.sortedBy {
            it.sort
        }
    }

    private fun actionsInit(actionList: List<ScriptActionInfo>,allActionMap : HashMap<Int,ScriptActionInfo>)  : ArrayList<ScriptActionInfo>{
        //从所有action中获取，避免重复初始化操作
        val resList = ArrayList<ScriptActionInfo>()
        actionList.forEach {
            val res = allActionMap.getOrPut(it.id) {
                initActionFun(it)
                it.intLabel?.let { label ->
                    val arr =  label.split(",").map { labelStr -> labelStr.toShort() }
                    it.intFirstLab = arr[0]
                    it.intLabelSet = arr.toSet()
                }
                it.intExcLabel?.let { label ->
                    it.intExcLabelSet = label.split(",").map { labelStr -> labelStr.toShort() }.toSet()
                }
                it.txtLabel?.let { label ->
                    val arr =  label.split("|")
                    it.txtFirstLab = arr[0].split(",").map { labelStr -> labelStr.toShort()  }.toSet()
                    it.txtLabelSet = arr.map { tmp->
                        tmp.split(",").map { labelStr -> labelStr.toShort() }.toSet()
                    }
                }
                it.txtExcLabel?.let{ label ->
                    it.txtExcLabelSet = label.split("|").map { tmp->
                        tmp.split(",").map { labelStr -> labelStr.toShort() }.toSet()
                    }
                }
                it
            }
            if(res.addFlag){
                resList.add(res)
            }
        }
        return resList
    }

    private fun tryAction(
        actionList: ArrayList<ScriptActionInfo>,
        detectLabels: Set<Short>,
        detectRes: Array<DetectResult>,
        txtLabels: Set<Short>,
        ocrRes: Array<OcrResult>) : Int{
        actionList.forEach scriptAction@{ sai ->
            if (sai.skipFlag || sai.flowId in skipFlowIds) {
                return@scriptAction
            }
            if (isMatch(sai, detectLabels,txtLabels)) {

                sai.command.onEach cmdForEach@{ cmd ->
                    when (cmd) {
                        is Return -> {
                            when (cmd.type) {
                                ActionString.FINISH -> {
                                    setScriptStatus(set, sai)
                                    println("结束：$sai.pageDesc")
                                    return 1
                                }
                            }
                        }
                        is Operation ->{
                            when (cmd.type) {
                                //点击
                                1 -> {
                                    val exeRes = execClick(sai,detectRes,ocrRes,cmd)
                                    if (exeRes in 4..5){
                                        return exeRes
                                    }
                                }
                            }
                        }
                        else -> {
                            //skip、sleep等
                            cmd.exec(sai)
                        }
                    }
                }
                println("找到${sai.pageDesc} || ${sai.flowId} || ${set.flowParentId}")
                return 2
            }
        }
        return 3
    }

    private fun tryBackAction(
        actionList: ArrayList<ScriptActionInfo>,
        detectLabels: Set<Short>,
        detectRes: Array<DetectResult>,
        txtLabels: Set<Short>,
        ocrRes: Array<OcrResult>
    ) : Int{
        actionList.forEach scriptAction@{ sai ->
            if (
                isMatch(sai,detectLabels, txtLabels)
            ) {
                sai.command.onEach cmdForEach@{ cmd ->
                    when (cmd) {
                        is Operation ->{
                            when(cmd.operation){
                                is AdbClick -> {
                                    execClick(sai,detectRes,ocrRes,cmd)
                                }
                            }
                        }
                    }
                }
                return 2
            }
        }
        return 3
    }


    private fun isMatch(sai: ScriptActionInfo, detectLabels: Set<Short>,txtLabels : Set<Short>) : Boolean{
        // 检查 intLabelSet 条件
        val int = sai.intLabelSet.isEmpty() || detectLabels.containsAll(sai.intLabelSet)

        // 检查 intExcLabelSet 条件
        val intExc = sai.intExcLabelSet.none { detectLabels.contains(it) }

        // 检查 txtLabelSet 条件
        val txt = sai.txtLabelSet.all { txtLabels.containsAll(it) }

        // 检查 txtExcLabelSet 条件
        val txtExc = sai.txtExcLabelSet.none { txtLabels.containsAll(it) }

        println("match: int${int}, intExc${intExc}, txt${txt}, txtExc${txtExc}")
        // 如果所有条件都满足
        return  int && intExc && txt && txtExc

    }

    private fun setScriptStatus(set : ScriptSetInfo, sai: ScriptActionInfo){
        if (set.backFlag ==0 && appDb.scriptRunStatusDao.countByFlowIdAndType(
                sai.scriptId,
                sai.flowId,
                set.flowIdType,
                dateTime = LocalDate.now()
                    .toString()
            ) == 0
        ) {
            val scriptStatus = ScriptRunStatus(
                scriptId = sai.scriptId,
                flowId = sai.flowId,
                flowIdType = set.flowIdType,
                curStatus = 2,
                dateTime = LocalDate.now()
                    .toString()
            )
            appDb.scriptRunStatusDao.insert(
                scriptStatus
            )
            println("非返回类，插入数据$scriptStatus")
        }
    }

    private fun isTimeOut(remTime : Long, limitTime : Long) : Boolean{
        return System.currentTimeMillis() - remTime > limitTime
    }
    /**
     * 1、在初次运行初始化
     * 2、在正常任务点击时更新
     * */
    private fun isToReboot(pkgName: String){
        if (isTimeOut(conf.remRebootTime , conf.rebootDelay)){
            partScope.launch { adbRebootApp(pkgName) }
        }
    }

    fun setPoints(sai: ScriptActionInfo, detectRes: Array<DetectResult>,ocrRes : Array<OcrResult>){
        if (sai.operTxt){
            val matchRes = ocrRes.filter {
                it.label.containsAll(sai.txtFirstLab)
            }.minByOrNull {
                it.label.size
            }
            globalSetMap.value[9]?.setValue?.let {
                if (matchRes != null){
                    sai.point = Point(matchRes.xCenter + it.toFloat(), matchRes.yCenter + it.toFloat())
                }
            }
        }else{
            val one = detectRes.firstOrNull {
                it.label == sai.intFirstLab
            }
            globalSetMap.value[9]?.setValue?.let {
                if (one != null){
                    sai.point = Point(one.xCenter + it.toFloat(), one.yCenter + it.toFloat())
                }
            }
        }

    }
    //操作映射为函数
    private fun initActionFun(scriptActionInfo: ScriptActionInfo){
        try {
            scriptActionInfo.addFlag = true
            scriptActionInfo.actionString.split(";").forEach {  action->
                //debug("action = ${action}")
                when(action){
                    ActionString.CLICK-> {
                        scriptActionInfo.command.add (Operation( 1, AdbClick()) )
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
                            action.startsWith(  ActionString.SKIP  ) -> {
                                val flowId = action.substring(   ActionString.SKIP.length+1, action.length-1   ).toInt()
                                scriptActionInfo.command.add(SkipFlowId(flowId))
                            }
                            action.startsWith( ActionString.CLICK_PART ) ->{
                                val type = if(action.contains("x")){"x"}else{"y"}
                                val lastIdx =  action.indexOfLast { it==',' }
                                val part = action.substring(   ActionString.CLICK_PART.length+3, lastIdx ).toInt()
                                val idx = action.substring(action.indexOfLast { it==',' }+1, action.length-1 ).toInt()
                                scriptActionInfo.command.add(Operation(1, AdbPartClick(type, part, idx)))
                            }
                            action.startsWith( ActionString.VER_SWIPE ) ->{
                                val type =  action.substring(   ActionString.VER_SWIPE.length ).toInt()
                                val dm = ScreenCaptureUtil.getDisplayMetrics(appCtx)
                                when(type){
                                    1 -> {
                                        val x =  (dm.widthPixels/4).toFloat()
                                        scriptActionInfo.swipePoint = Rect( x, (dm.heightPixels/8 * 3).toFloat(), x,  (dm.heightPixels/8 * 5).toFloat()  )
                                    }
                                    2 ->{
                                        val x =  (dm.widthPixels/4).toFloat()
                                        scriptActionInfo.swipePoint = Rect( x,  (dm.heightPixels/8 * 5).toFloat(), x,  (dm.heightPixels/8 * 3).toFloat()  )
                                    }
                                    3 ->{
                                        val y =  (dm.heightPixels/4).toFloat()
                                        scriptActionInfo.swipePoint = Rect(  (dm.widthPixels/4).toFloat(), y,  (dm.widthPixels/4 * 3).toFloat(), y  )
                                    }
                                    4 ->{
                                        val y =  (dm.heightPixels/4).toFloat()
                                        scriptActionInfo.swipePoint = Rect( (dm.widthPixels/4 * 3).toFloat() , y,  (dm.widthPixels/4).toFloat(), y  )
                                    }
                                    5->{
                                        val x =  (dm.widthPixels/4 * 3).toFloat()
                                        scriptActionInfo.swipePoint = Rect( x, (dm.heightPixels/8 * 3).toFloat(), x,  (dm.heightPixels/8 * 5).toFloat()  )
                                    }
                                    6 ->{
                                        val x =  (dm.widthPixels/4 * 3).toFloat()
                                        scriptActionInfo.swipePoint = Rect( x, (dm.heightPixels/8 * 5).toFloat(), x,   (dm.heightPixels/8 * 3).toFloat()  )
                                    }
                                    7->{
                                        val y =  (dm.heightPixels/4 * 3).toFloat()
                                        scriptActionInfo.swipePoint = Rect(  (dm.widthPixels/4).toFloat(), y,  (dm.widthPixels/4 * 3).toFloat(), y  )
                                    }
                                    8->{
                                        val y =  (dm.heightPixels/4 * 3).toFloat()
                                        scriptActionInfo.swipePoint = Rect(  (dm.widthPixels/4 * 3).toFloat(), y,  (dm.widthPixels/4).toFloat() , y  )
                                    }
                                }
                            }
                            action.startsWith( ActionString.HOR_SWIPE ) ->{
                                val type =  action.substring(   ActionString.HOR_SWIPE.length ).toInt()
                                val dm = ScreenCaptureUtil.getDisplayMetrics(appCtx)
                                when(type){
                                    1 -> {
                                        val x =  (dm.widthPixels/8 ).toFloat()
                                        scriptActionInfo.swipePoint = Rect( x, (dm.heightPixels/4).toFloat(), x,  (dm.heightPixels/2).toFloat() )
                                    }
                                    2 ->{
                                        val x =  (dm.widthPixels/8 ).toFloat()
                                        scriptActionInfo.swipePoint = Rect( x,  (dm.heightPixels/2).toFloat() , x, (dm.heightPixels/4 ).toFloat()   )
                                    }
                                    3 ->{
                                        val y =  (dm.heightPixels/4).toFloat()
                                        scriptActionInfo.swipePoint = Rect(  (dm.widthPixels/8 * 3).toFloat(), y,  (dm.widthPixels/8 * 5).toFloat(), y  )
                                    }
                                    4 ->{
                                        val y =  (dm.heightPixels/4).toFloat()
                                        scriptActionInfo.swipePoint = Rect( (dm.widthPixels/8 * 5).toFloat() , y,  (dm.widthPixels/8 * 3).toFloat(), y  )
                                    }
                                    5->{
                                        val x =  (dm.widthPixels/8 * 7).toFloat()
                                        scriptActionInfo.swipePoint = Rect( x, (dm.heightPixels/4 ).toFloat(), x,  (dm.heightPixels/2 ).toFloat()  )
                                    }
                                    6 ->{
                                        val x =  (dm.widthPixels/8 * 7).toFloat()
                                        scriptActionInfo.swipePoint = Rect( x, (dm.heightPixels/2 ).toFloat(), x,   (dm.heightPixels/4 ).toFloat()  )
                                    }
                                    7->{
                                        val y =  (dm.heightPixels/4 * 3).toFloat()
                                        scriptActionInfo.swipePoint = Rect(  (dm.widthPixels/8 * 3).toFloat(), y,  (dm.widthPixels/8 * 5).toFloat(), y  )
                                    }
                                    8->{
                                        val y =  (dm.heightPixels/4 * 3).toFloat()
                                        scriptActionInfo.swipePoint = Rect(  (dm.widthPixels/8 * 5).toFloat(), y,  (dm.widthPixels/8 * 3).toFloat() , y  )
                                    }
                                }
                            }
                            action.startsWith( ActionString.UN_CHECKED )->{
                                val flowIds = action.substring( ActionString.UN_CHECKED.length+1, action.length-1).split(",").map { it.toInt() }
                                if(appDb.scriptSetInfoDao.countCheckedNumByParentFlowId(scriptActionInfo.scriptId , flowIds) > 0){
                                    scriptActionInfo.addFlag = false
                                    return
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
            println(detectRes.toList().toString())
        }
        detectLabels?.let {
            println("detectLabels.size："+detectLabels.size)
            println( detectLabels.toList().toString())
        }
    }

/*    fun updateScript(scriptInfo: ScriptInfo){
        checkAndRestartScopeState()
        scriptRunCoroutineScope.launch {
            appDb.scriptInfoDao.update(scriptInfo)
        }
    }
    fun updateScriptSet(scriptSetInfo: ScriptSetInfo){
        checkAndRestartScopeState()
        scriptRunCoroutineScope.launch {
            appDb.scriptSetInfoDao.update(scriptSetInfo)
        }
    }*/
}
