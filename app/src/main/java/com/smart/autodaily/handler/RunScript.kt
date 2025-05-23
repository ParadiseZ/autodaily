package com.smart.autodaily.handler

import androidx.collection.mutableIntSetOf
import androidx.compose.runtime.mutableIntStateOf
import com.smart.autodaily.command.AdbBack
import com.smart.autodaily.command.AdbClick
import com.smart.autodaily.command.AdbPartClick
import com.smart.autodaily.command.AdbSwipe
import com.smart.autodaily.command.AddPosById
import com.smart.autodaily.command.DropdownMenuNext
import com.smart.autodaily.command.FinishFlowId
import com.smart.autodaily.command.MinusPosById
import com.smart.autodaily.command.NotFlowId
import com.smart.autodaily.command.Operation
import com.smart.autodaily.command.Reboot
import com.smart.autodaily.command.Return
import com.smart.autodaily.command.RmSkipAcId
import com.smart.autodaily.command.RmSkipAcIdList
import com.smart.autodaily.command.RmSkipFlowId
import com.smart.autodaily.command.RootExecutor
import com.smart.autodaily.command.ShellConfig
import com.smart.autodaily.command.ShizukuExecutor
import com.smart.autodaily.command.Skip
import com.smart.autodaily.command.SkipAcId
import com.smart.autodaily.command.SkipFlowId
import com.smart.autodaily.command.Sleep
import com.smart.autodaily.command.adbRebootApp
import com.smart.autodaily.command.adbStartApp
import com.smart.autodaily.constant.ActionString
import com.smart.autodaily.constant.MODEL_BIN
import com.smart.autodaily.constant.MODEL_PARAM
import com.smart.autodaily.constant.WORK_TYPE00
import com.smart.autodaily.constant.WORK_TYPE01
import com.smart.autodaily.constant.WORK_TYPE02
import com.smart.autodaily.constant.WORK_TYPE03
import com.smart.autodaily.data.appDb
import com.smart.autodaily.data.entity.ConfigData
import com.smart.autodaily.data.entity.DetectResult
import com.smart.autodaily.data.entity.Point
import com.smart.autodaily.data.entity.Rect
import com.smart.autodaily.data.entity.ScriptActionInfo
import com.smart.autodaily.data.entity.ScriptInfo
import com.smart.autodaily.data.entity.ScriptRunStatus
import com.smart.autodaily.data.entity.ScriptSetInfo
import com.smart.autodaily.data.entity.ScriptSetRunStatus
import com.smart.autodaily.navpkg.AutoDaily
import com.smart.autodaily.utils.Lom
import com.smart.autodaily.utils.ScreenCaptureUtil
import com.smart.autodaily.utils.ServiceUtil
import com.smart.autodaily.utils.ShizukuUtil
import com.smart.autodaily.utils.SnackbarUtil
import com.smart.autodaily.utils.getMd5Hash
import com.smart.autodaily.utils.getPicture
import com.smart.autodaily.utils.isBetweenHour
import com.smart.autodaily.utils.partScope
import com.smart.autodaily.utils.rgbToHsv
import com.smart.autodaily.utils.runScope
import com.smart.autodaily.viewmodel.workType
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
val skipAcIds  by lazy {
    mutableIntSetOf()
}
val model by lazy{
    AutoDaily()
}

val allActionMap : HashMap<Int,ScriptActionInfo> by lazy{
    hashMapOf()
}

const val INFO = "info"
const val ERROR = "error"
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

    suspend fun runScript(){
        workType.value = globalSetMap.value[8]?.setValue ?:""
        workType.value.let {
            when(it) {
                WORK_TYPE00 ->{
                    SnackbarUtil.show("未设置工作模式！")
                    isRunning.intValue = 0
                }
                WORK_TYPE01 -> {
                    isRunning.intValue = 0
                }
                WORK_TYPE02 -> {
                    //_isRunning.value = 2//启动服务
                    ServiceUtil.runUserService(appCtx)
                    initScriptData(appDb.scriptInfoDao.getAllScriptByChecked())
                    ServiceUtil.waitShizukuService()
                    if(ShizukuUtil.grant && ShizukuUtil.iUserService != null){
                        isRunning.intValue = 1//运行中
                        Lom.waitWriteLog()
                        Lom.n( INFO, "初始化全局设置" )
                        initConfData()
                        ShellConfig.useRoot = false
                        conf.executor = ShizukuExecutor()
                        runScriptByAdb()
                        isRunning.intValue = 0
                    }else{
                        isRunning.intValue = 0//启动服务失败
                        SnackbarUtil.show("请检查shizuku服务！")
                        return
                    }
                    //(manActivityCtx as MainActivity).requestOverlayPermission()
                }

                WORK_TYPE03 -> {
                    Lom.waitWriteLog()
                    Lom.n( INFO, "初始化全局设置" )
                    initConfData()
                    runScriptByAdb()
                    ShellConfig.useRoot = true
                    conf.executor = RootExecutor()
                    isRunning.intValue = 0
                }
                else-> {
                    isRunning.intValue = 0
                }
            }
        }
    }
    private fun initConfData(){
        conf = ConfigData(
            _globalSetMap.value[3]?.setValue?.toFloat()?.times(1000)?.toLong()?:1500L,
            _globalSetMap.value[59]?.checkedFlag == true,
            _globalSetMap.value[5]?.run { this.setValue?.toFloat() }?:0.5f,
            _globalSetMap.value[4]?.run { this.setValue?.toFloat()?.times(60000)?.toLong() }?: 600000L,
            _globalSetMap.value[10]?.checkedFlag == true,
            _globalSetMap.value[1]?.checkedFlag == true,
            globalSetMap.value[9]?.setValue?.toFloat()?:0f,
            _globalSetMap.value[6]?.setValue?.toInt()?.let { it * 32 }?:640,
            remRebootTime = System.currentTimeMillis(),
            10000L,
            2,
            ScreenCaptureUtil.getDisplayMetrics(appCtx).run {
                this.widthPixels.coerceAtMost(this.heightPixels)
            },
            null
        )
    }
    //shell运行
    suspend fun runScriptByAdb(){
        if(conf.minScreen  > 720){
            conf.capScale = 2
        }
        Lom.d("config", conf.toString())
        _scriptCheckedList.value.forEach scriptForEach@{ si->
            skipFlowIds.clear()
            skipAcIds.clear()
            allActionMap.clear()
            conf.pkgName = si.packageName
            //保存的所有的action map

            if(si.currentRunNum < si.runsMaxNum){
                Lom.n( INFO, si.scriptName )
                startApp( conf.pkgName )
                if(!loadModel(si)){
                    isRunning.intValue = 0
                    runScope.coroutineContext.cancelChildren()
                    return
                }
                //insertFirstDetect(si.classesNum, si.packageName)
                //所有选择的set
                val scriptSet = getScriptSets(si.scriptId)
                if (conf.recordStatus){
                    val date = LocalDate.now().toString()
                    if(appDb.scriptRunStatusDao.countByFlowIdAndType(si.scriptId, scriptSet[0].flowIdType, date) > 0 ){
                        Lom.n(INFO, "当前时间段已执行，跳过：${si.scriptName}")
                        return@scriptForEach
                    }
                }
                /*println("scriptSet")
                scriptSet.forEach {
                    println(it)
                }*/
                val backActionArrayList : ArrayList<ScriptActionInfo> = arrayListOf()
                if (conf.tryBackAction){
                    backActionArrayList.addAll(
                        actionsInit(appDb.scriptActionInfoDao.getBackActions(si.scriptId), allActionMap)
                    )
                    Lom.d(INFO, "返回操作数量${backActionArrayList.size}")
                }
                Lom.d( INFO, "详细设置初始化完毕" )
                scriptSet.forEach setForEach@ { forSet->
                    set = forSet
                    //跳跃或有今天的执行记录，则遍历下一条
                    if(conf.recordStatus && appDb.scriptSetRunStatusDao.countByFlowIdAndType(set.scriptId,set.flowId!!, set.flowIdType, LocalDate.now().toString()) > 0 ){
                        Lom.n(INFO, "当前时间段已执行，跳过：${set.setName}")
                        return@setForEach
                    }
                    Lom.n(INFO, "本次任务：${set.setId} ${set.setName}")
                    //遍历的操作合集
                    val scriptAction = appDb.scriptActionInfoDao.getCheckedBySetId( set.scriptId, set.flowParentIdList, set.flowIdType )
                    val scriptActionArrayList = actionsInit(scriptAction,allActionMap)
                    Lom.d( INFO, "操作初始化完毕，准备截图识别" )
                    //遍历的返回操作合集
                    while (isRunning.intValue == 1){
                        //超时重启
                        isToReboot(si.packageName)
                        val time1 = System.currentTimeMillis()
                        //截图延迟
                        val capture = if (conf.capture == null || conf.capture!!.isRecycled) {
                                        getPicture(conf.capScale)?:continue
                                    } else conf.capture!!
                        Lom.d( ERROR, "截图耗时${System.currentTimeMillis()-time1}" )

                        val detectRes = model.detectYolo(capture, si.classesNum).filter { it!=null }//.filter { it.prob > conf.similarScore }
                            .toTypedArray()
                        val detectLabels = detectRes.filter { it.label> 0 }.map { it.label  }.toSet()
                        val txtRes = detectRes.filter { it.label == 0 }
                        val txtLabels = if(txtRes.isEmpty()){ arrayOf() }else{
                            txtRes.map {
                                it.ocr!!.label = it.ocr.labelArr.toSet()
                                it.ocr.colorSet = it.ocr.colorArr.toSet()
                                it.ocr.label
                            }.toTypedArray()
                        }
                        //MD5计算
                        conf.beforeHash = getMd5Hash(capture)
                        //释放截图
                        // 使用BitmapPool回收Bitmap对象而不是直接调用recycle
                        com.smart.autodaily.utils.BitmapPool.recycle(conf.capture)
                        conf.capture = null
                        //debugPrintScriptActionLabels(detectRes, detectLabels)
                        if (detectLabels.isEmpty() && txtLabels.isEmpty()) {
                            //Lom.d(INFO,"未识别到内容")
                            continue
                        }
                        if(Lom.enableConsole){
                            for (result in detectRes.filter { it.label == 0 }) {
                                print(result.ocr?.txt+" ")
                                print(result.ocr?.label?.toList())
                                println(result.ocr?.colorArr?.toList())
                            }
                            println(detectLabels.toList())
                        }
                        /*if(enableLog && logSet.value=="详细"){
                            for (result in detectRes.filter { it.label == 0 }) {
                                Lom.d("Text:",result.ocr?.txt+",Label:"+result.ocr?.label?.toList()?.plus(" ").toString() + ",Color:"+result.ocr?.colorArr?.toList().toString())
                            }
                            Lom.d("DetectLabel","${detectLabels.toList()}")
                        }
*/

                        //conf.curHash = getMd5Hash(capture)
                        when(tryAction(scriptActionArrayList, detectLabels, detectRes, txtLabels)){
                            1 ->{
                                //finish类
                                Lom.n(INFO, "结束:${set.setName}")
                                // 使用BitmapPool回收Bitmap对象而不是直接调用recycle
                                com.smart.autodaily.utils.BitmapPool.recycle(conf.capture)
                                conf.capture = null
                                return@setForEach
                            }
                            2 ->{
                                //整个操作执行结束
                            }
                            3 ->{
                                Lom.d(INFO,"当前任务 ${set.setId} [${set.setName}]未匹配到")
                                if (conf.tryBackAction){
                                    Lom.d(INFO,"准备尝试返回")
                                    tryBackAction( backActionArrayList, detectLabels,detectRes, txtLabels)
                                }
                            }
                            4 ->{
                                //操作无效/找到未操作（和操作前对比界面无变化）
                                Lom.n(INFO, "点击操作失败，已达到最大重试次数")
                            }
                            5->{
                                //点无效
                            }
                        }
                        delay(conf.intervalTime)
                    }
                }//set for each
                // 使用BitmapPool回收Bitmap对象而不是直接调用recycle
                com.smart.autodaily.utils.BitmapPool.recycle(conf.capture)
                conf.capture = null
                //记录是否完成
                if (conf.recordStatus){
                    setScriptStatus(set)
                }
                Lom.n(INFO , "${si.scriptName} 结束")
                return@scriptForEach
            }else{
                si.currentRunNum = 0
                si.nextRunDate = LocalDate.now().toString()
                //appDb.scriptInfoDao.update(si)
            }
        }
        isRunning.intValue = 0
    }

    suspend fun deleteRunStatus(){
        val d : String = LocalDate.now().minusDays(7).toString()
        appDb.scriptRunStatusDao.deleteStatus(d)
        appDb.scriptSetRunStatusDao.deleteStatus(d)
    }

    //启动APP
    private fun startApp(pkgName : String){
        try {
            partScope.launch {
                adbStartApp(pkgName)
            }
        }catch (_ : Exception){
            Lom.n( ERROR, "app启动失败！")
            SnackbarUtil.show("app启动失败！")
            return
        }
    }

    private fun loadModel(si  : ScriptInfo) : Boolean{
        var loadRes = false
        appCtx.getExternalFilesDir("")?.let {
            model.loadModelSec(appCtx.assets,
                it.path+"/"+si.modelPath+"/"+ MODEL_PARAM,
                it.path+"/"+si.modelPath+"/"+ MODEL_BIN,
                si.imgSize,  conf.useGpu, 16, 0, true, true)
            loadRes = true
        }?:{
            SnackbarUtil.show("加载模型失败!")
        }
        Lom.d(INFO,"加载模型成功！")
        return loadRes
    }

    private fun getScriptSets(scriptId : Int) : List<ScriptSetInfo>{
        //val curFlowId = appDb.scriptActionInfoDao.getCurFlowIdById(appDb.labelFtsDao.getMaxIdFromCurrent())
        val curFlowId = 0
        //最小子项
        val maxSet = appDb.scriptSetInfoDao.getScriptSetByScriptId(scriptId, curFlowId, 1)
        //最小子项的上级or项
        val orSet = appDb.scriptSetInfoDao.getScriptSetByScriptId(scriptId, curFlowId, 2)
        orSet.forEach{
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
                    val arr =  label.split(",").map { labelStr -> labelStr.toInt() }
                    it.intFirstLab = arr[0]
                    it.intLabelSet = arr.toSet()
                }
                it.intExcLabel?.let { label ->
                    it.intExcLabelSet = label.split(",").map { labelStr -> labelStr.toInt() }.toSet()
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
                it.rgb?.let { color->
                    it.hsv = color.split(";").map { rgbStr->
                        val co = rgbStr.split(",").map { rgb-> rgb.toShort() }.toList()
                        val hsv = rgbToHsv(co)
                        model.hsvToColor(hsv[0], hsv[1], hsv[2])
                    }.toSet()
                }
                /*it.rgbExc?.let { color->
                    it.hsvExc = color.split(";").map { rgbStr->
                        val co = rgbStr.split(",").map { rgb-> rgb.toShort() }.toList()
                        val hsv = rgbToHsv(co)
                        ModelUtil.model.hsvToColor(hsv[0], hsv[1], hsv[2]).toShort()
                    }.toSet()
                }*/
                it
            }
            if(res.addFlag){
                resList.add(res)
            }
        }
        return resList
    }

    private suspend fun tryAction(
        actionList: ArrayList<ScriptActionInfo>,
        detectLabels: Set<Int>,
        detectRes: Array<DetectResult>,
        txtLabels: Array<Set<Short>>
    ) : Int{
        actionList.forEach scriptAction@{ sai ->
            if (sai.skipFlag || sai.flowId in skipFlowIds || sai.id in skipAcIds) {
                Lom.n(INFO, "跳过${sai.pageDesc}")
                return@scriptAction
            }
            if (isMatch(sai, detectLabels,txtLabels) && checkColor(sai,detectRes)) {
                conf.remRebootTime = System.currentTimeMillis()
                sai.pageDesc?.let { Lom.n(INFO, "✔\uFE0F【${it}】") }
                sai.command.onEach cmdForEach@{ cmd ->
                    when (cmd) {
                        is Operation ->{
                            when (cmd.type) {
                                //点击
                                1 -> {
                                    val exeRes = execClick(sai,detectRes,cmd)
                                    if (exeRes in 4..5){
                                        return exeRes
                                    }
                                }
                                //点击中央
                                2 -> {
                                    Lom.n(INFO , "点击 ${(cmd.operation as AdbClick).point}")
                                    cmd.exec(sai)
                                }
                                //滑动
                                3 ->{
                                    sai.swipePoint?.let {
                                        Lom.n(INFO , "滑动 ${it.x},${it.y}->${it.width},${it.height}")
                                    }
                                    if (sai.executeMax > 0){
                                        sai.executeCur += 1
                                        Lom.d(INFO, "第${sai.executeCur}次滑动结束")
                                        if (sai.executeCur >= sai.executeMax) {
                                            Lom.d(INFO, "已达到最大滑动次数，设置跳过")
                                            sai.executeCur = 0
                                            sai.skipFlag = true
                                        }
                                    }
                                    cmd.exec(sai)
                                }
                            }
                        }
                        is Return -> {
                            when (cmd.type) {
                                ActionString.FINISH -> {
                                    if (conf.recordStatus){
                                        setScriptSetStatus(set, sai, sai.flowId)
                                    }
                                    return 1
                                }
                            }
                        }
                        is FinishFlowId ->{
                            Lom.n(INFO , "结束流程ID ${cmd.flowId}")
                            setScriptSetStatus(set, sai, cmd.flowId)
                        }
                        is NotFlowId->{
                            if (set.flowId == cmd.notFlowId){
                                skipAcIds.add(sai.id)
                                return@scriptAction
                            }
                        }
                        is AddPosById->{
                            actionList.forEach { tmp ->
                                if (tmp.id == cmd.saiId){
                                    Lom.n(INFO , "saiId ${cmd.saiId}点击目标向后递增")
                                    tmp.labelPos += 1
                                }
                            }
                        }
                        is DropdownMenuNext->{
                            partScope.launch {
                                val set = appDb.scriptSetInfoDao.getScriptSetById(cmd.setId)
                                if (set!=null && set.setDefaultValue!=null && set.setValue!=null){
                                    val allSetValue = set.setDefaultValue.split(",")
                                    val curIdx = allSetValue.indexOf(set.setValue)
                                    val nextIndex = if (curIdx == -1) 0 else (curIdx + 1) % allSetValue.size
                                    Lom.n(INFO ,"设置[${set.setName}]由${set.setValue}更改为${allSetValue[nextIndex]}")
                                    set.setValue = allSetValue[nextIndex]
                                    appDb.scriptSetInfoDao.update(set)
                                }
                            }
                        }
                        is MinusPosById->{
                            actionList.forEach { tmp ->
                                if (tmp.id == cmd.saiId){
                                    tmp.labelPos -= 1
                                }

                            }
                        }
                        is Reboot->{
                            Lom.n(INFO , "重启中...")
                            cmd.exec(sai)
                        }
                        else -> {
                            //skip、sleep等
                            cmd.exec(sai)
                        }
                    }
                }
                return 2
            }
        }
        return 3
    }

    private fun tryBackAction(
        actionList: ArrayList<ScriptActionInfo>,
        detectLabels: Set<Int>,
        detectRes: Array<DetectResult>,
        txtLabels: Array<Set<Short>>
    ) : Int{
        actionList.forEach scriptAction@{ sai ->
            if (
                isMatch(sai,detectLabels, txtLabels)
            ) {
                conf.remRebootTime = System.currentTimeMillis()
                sai.pageDesc?.let { Lom.n(INFO, "(｡>‿‿<｡)【$it】") }
                sai.command.onEach cmdForEach@{ cmd ->
                    when (cmd) {
                        is Operation ->{
                            when(cmd.type){
                                1 -> {
                                    execClick(sai,detectRes,cmd)
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


    private fun isMatch(sai: ScriptActionInfo, detectLabels: Set<Int>, txtLabels: Array<Set<Short>>) : Boolean{
        // 检查 intLabelSet 条件
        val int = sai.intLabelSet.isEmpty() || detectLabels.containsAll(sai.intLabelSet)

        // 检查 intExcLabelSet 条件
        val intExc = if (int) sai.intExcLabelSet.none { detectLabels.contains(it) } else return false

        // 检查 txtLabelSet 条件
        val txt = if(intExc) sai.txtLabelSet.all { expect ->
            txtLabels.any {
                it.containsAll(expect)  && it.size < expect.size*3
            }
        } else return false
        // 检查 txtExcLabelSet 条件
        val txtExc = if(txt) sai.txtExcLabelSet.all { except->
            txtLabels.none { it.containsAll(except) }
        }else return false
        /*if (sai.id == 181){
            print(int)
            print(intExc)
            print(txt)
            println(txtExc)
            txtLabels.forEach {
                println(it)
            }
            sai.txtLabel?.forEach {
                println(it)
            }
        }*/
        // 如果所有条件都满足
        return txtExc
        //return  int && intExc && txt
    }

    private suspend fun setScriptStatus(set : ScriptSetInfo){
        if (appDb.scriptRunStatusDao.countByFlowIdAndType(
                set.scriptId,
                set.flowIdType,
                dateTime = LocalDate.now()
                    .toString()
            ) == 0
        ) {
            val scriptStatus = ScriptRunStatus(
                scriptId = set.scriptId,
                flowIdType = set.flowIdType,
                curStatus = 2,
                dateTime = LocalDate.now()
                    .toString()
            )
            appDb.scriptRunStatusDao.insert(
                scriptStatus
            )
            Lom.d(INFO, "\uD83D\uDCDD 运行结束 ${set.scriptId}")
        }
    }

    /*
    *设置设置运行状态的表，暂未使用
     * */
    private suspend fun setScriptSetStatus(set : ScriptSetInfo, sai: ScriptActionInfo, flowId : Int){
        if (set.backFlag ==0 && appDb.scriptSetRunStatusDao.countByFlowIdAndType(
                sai.scriptId,
                flowId,
                set.flowIdType,
                dateTime = LocalDate.now()
                    .toString()
            ) == 0
        ) {
            val scriptStatus = ScriptSetRunStatus(
                scriptId = sai.scriptId,
                flowId = flowId,
                flowIdType = set.flowIdType,
                curStatus = 2,
                dateTime = LocalDate.now()
                    .toString()
            )
            appDb.scriptSetRunStatusDao.insert(
                scriptStatus
            )
            Lom.d(INFO, "\uD83D\uDCDD完成流程id${flowId}")
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
            Lom.n( INFO, "识别超时，准备重启")
            conf.remRebootTime = System.currentTimeMillis()
            partScope.launch { adbRebootApp(pkgName) }
        }
    }
    //操作映射为函数
    private fun initActionFun(scriptActionInfo: ScriptActionInfo){
        try {
            scriptActionInfo.addFlag = true
            scriptActionInfo.actionString.split(";").forEach {  action->
                when(action){
                    ActionString.CLICK-> {
                        scriptActionInfo.command.add (Operation( 1, AdbClick()) )
                    }
                    ActionString.CLICKC-> {
                        val x = (ScreenCaptureUtil.getDisplayMetrics(appCtx).widthPixels/2 + conf.random).toInt()
                        val y = (ScreenCaptureUtil.getDisplayMetrics(appCtx).heightPixels/2 + conf.random).toInt()
                        scriptActionInfo.command.add (Operation( 2, AdbClick(Point(x,y))) )
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
                    ActionString.REBOOT ->{
                        scriptActionInfo.command.add(Reboot(conf.pkgName))
                    }
                    else ->{
                        when{
                            action.startsWith(  ActionString.SLEEP  ) -> {
                                val sleepTime = action.substring(   ActionString.SLEEP.length+1, action.length-1   ).toLong()
                                scriptActionInfo.command.add(Sleep(sleepTime))
                            }
                            action.startsWith(  ActionString.FINISH  ) -> {
                                val flowId = action.substring(   ActionString.FINISH.length+1, action.length-1   ).toInt()
                                scriptActionInfo.command.add(FinishFlowId(flowId))
                            }
                            action.startsWith(  ActionString.SKIP_FLOW_ID  ) -> {
                                val flowId = action.substring(   ActionString.SKIP_FLOW_ID.length+1, action.length-1   ).toInt()
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
                                scriptActionInfo.command.add(Operation(3, AdbSwipe()))
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
                                scriptActionInfo.command.add(Operation(3, AdbSwipe()))
                            }
                            action.startsWith( ActionString.UN_CHECKED )->{
                                val flowIds = action.substring( ActionString.UN_CHECKED.length+1, action.length-1).split(",").map { it.toInt() }
                                if(appDb.scriptSetInfoDao.countCheckedNumByParentFlowId(scriptActionInfo.scriptId , flowIds) > 0){
                                    scriptActionInfo.addFlag = false
                                    return
                                }
                            }
                            action.startsWith( ActionString.NOT_FLOWID) ->{
                                val flowId = action.substring(   ActionString.NOT_FLOWID.length+1, action.length-1   ).toInt()
                                scriptActionInfo.command.add(NotFlowId(flowId))
                            }
                            action.startsWith( ActionString.RM_FLOW_ID) ->{
                                val flowId = action.substring(   ActionString.RM_FLOW_ID.length+1, action.length-1   ).toInt()
                                scriptActionInfo.command.add(RmSkipFlowId(flowId))
                            }
                            action.startsWith( ActionString.SKIP_ACID) ->{
                                val acId = action.substring(   ActionString.SKIP_ACID.length+1, action.length-1   ).toInt()
                                scriptActionInfo.command.add(SkipAcId(acId))
                            }
                            action.startsWith( ActionString.RM_SKIP_ACID) ->{
                                val acId = action.substring(   ActionString.RM_SKIP_ACID.length+1, action.length-1   ).toInt()
                                scriptActionInfo.command.add(RmSkipAcId(acId))
                            }
                            action.startsWith( ActionString.POS_ADD) ->{
                                val saiId = action.substring(   ActionString.POS_ADD.length+1, action.length-1   ).toInt()
                                scriptActionInfo.command.add(AddPosById(saiId))
                            }
                            action.startsWith( ActionString.POS_MINUS) ->{
                                val saiId = action.substring(   ActionString.POS_MINUS.length+1, action.length-1   ).toInt()
                                scriptActionInfo.command.add(MinusPosById(saiId))
                            }
                            action.startsWith(ActionString.DROP_SET_NEXT) ->{
                                val setId = action.substring(ActionString.DROP_SET_NEXT.length+1, action.length-1).toInt()
                                scriptActionInfo.command.add(DropdownMenuNext(setId))
                            }
                            action.startsWith(ActionString.RM_SKIP_ACIDS) ->{
                                val acIds = mutableIntSetOf()
                                action.substring(ActionString.RM_SKIP_ACIDS.length+1, action.length-1).split(",").map {
                                    acIds.add(it.toInt())
                                }
                                scriptActionInfo.command.add(RmSkipAcIdList(acIds))
                            }
                        }
                    }
                }
            }
        }catch (_ : Exception){
            Lom.n(ERROR, "initActionFun error:${scriptActionInfo}")
            isRunning.intValue = 0
            runScope.coroutineContext.cancelChildren()
            SnackbarUtil.show("初始化action失败，请联系管理员！")
            return
        }
    }

    //匹配
    //初始化已选择脚本数据，HomeScreen调用
    fun initScriptData(scriptList : List<ScriptInfo>){
        this._scriptCheckedList.value = scriptList
    }
}
