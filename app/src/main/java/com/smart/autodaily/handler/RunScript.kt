package com.smart.autodaily.handler

import com.smart.autodaily.command.AdbClick
import com.smart.autodaily.command.AdbSumClick
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
import com.smart.autodaily.utils.ScreenCaptureUtil
import com.smart.autodaily.utils.ShizukuUtil
import com.smart.autodaily.utils.debug
import com.smart.autodaily.utils.toastOnUi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.opencv.android.Utils
import org.opencv.core.KeyPoint
import org.opencv.core.Mat
import org.opencv.core.MatOfDMatch
import org.opencv.core.MatOfKeyPoint
import org.opencv.features2d.BFMatcher
import org.opencv.features2d.ORB
import splitties.init.appCtx
import java.util.Date


object  RunScript {

    private val _scriptCheckedList = MutableStateFlow<List<ScriptInfo>>(emptyList())

    private val _globalSetMap = MutableStateFlow<Map<Int, ScriptSetInfo>>(emptyMap())
    val globalSetMap : StateFlow<Map<Int, ScriptSetInfo>> get() = _globalSetMap

    //val scriptCheckedList : StateFlow<List<ScriptInfo>> = _scriptCheckedList
    //var scriptSetList : List<ScriptInfo> = emptyList()
    //val globalSetList =  MutableStateFlow<List<ScriptSetInfo>>(emptyList())
    private var scriptRunCoroutineScope = CoroutineScope(Dispatchers.IO)
    private var sourceMat = Mat()
    private val orb: ORB = ORB.create()



    private fun initOrb(){
        orb.maxFeatures = 10//要检测的最大特征数量
        //多尺度金字塔中的相邻层之间使用的缩放因子。较高的值意味着较少的尺度层级。为2时，每个级别的像素是前一个级别的1/4。
        orb.scaleFactor = 1.5
        orb.nLevels = 3 //特征检测的层级数
        orb.edgeThreshold = 10 //边缘检测阈值,间隔多少像素开始检测
        //orb.firstLevel=0//多尺度金字塔中的第一个（最粗的）图像的初始等级。默认情况下，这通常是0，表示原始图像。
        orb.wtA_K =3 //BRIEF描述子的长度，可以是2或3。2，则描述符长度为31个字节；3，则描述符长度为32个字节。
        orb.scoreType = ORB.FAST_SCORE //质量评分标准HARRIS_SCORE角点检测
        orb.patchSize = 3 //描述符的邻域大小。这是以像素为单位的正方形区域的边长
        //orb.fastThreshold = 1 //FAST角点检测器的阈值。FAST检测器会寻找对比度超过该阈值的角点。
    }

    fun initGlobalSet(){
        appDb!!.scriptSetInfoDao.getGlobalSet().associateBy {
            it.setId
        }.let {
            this._globalSetMap.value = it
        }
    }

    fun runScript(scriptSetInfo: ScriptSetInfo) {
        //initOrb()
        //已选脚本
        scriptRunCoroutineScope.launch {
            when(scriptSetInfo.setValue){
                WORK_TYPE01 ->{}
                WORK_TYPE02 -> {
                    runScriptByAdb()
                }
                WORK_TYPE03 -> {}
            }
            /*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                runScriptByAdb()
            }*/
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
                }catch (e: Exception){
                    appCtx.toastOnUi("app启动失败！")
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
                        val captureBitmap = ShizukuUtil.iUserService?.execCap("screencap -p")
                        Utils.bitmapToMat(captureBitmap , sourceMat)//截图bitmap到mat
                        if (captureBitmap != null) {
                            scriptActionList.forEach scriptAction@{
                                if (!it.skipFlag) {
                                    //寻找，目的为找到，所有的都找到则继续
                                    println("picNeedFindList:${it.picNeedFindList}")
                                    if(templateMatch(si.picPath, it.picNeedFindList, sourceMat, _globalSetMap.value[5]!!.setValue!!.toDouble(), true,it,true)){
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
                                                        if(!templateMatch(si.picPath, it.picNotNeedFindList, sourceMat, _globalSetMap.value[5]!!.setValue!!.toDouble(), true,it,false)){
                                                            return@scriptAction
                                                        }
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
    private fun findTarget(picPath : String,picNameList : List<String>?, sai : ScriptActionInfo, keypointsSourceList : List<KeyPoint>, descriptorsSource : Mat, matchSave : Boolean) : Boolean{
        // 计算匹配点的平均位置
        var avgX = 0.0
        var avgY = 0.0
        picNameList?.forEach {
            println("picPath:$picPath/$it.png")
            val targetMat = getPicture("$picPath/$it.png")
            val keypointsTarget = MatOfKeyPoint()
            val descriptorsTarget = Mat()
            orb.detectAndCompute(targetMat, Mat(), keypointsTarget, descriptorsTarget)
            val matcher = BFMatcher.create(BFMatcher.BRUTEFORCE_HAMMINGLUT)
            val matchesMat = MatOfDMatch()
            println("descriptorsTarget:${descriptorsTarget.size()}")
            println("descriptorsSource:${descriptorsSource.size()}")

            matcher.match(descriptorsSource, descriptorsTarget, matchesMat)
            val matches = matchesMat.toList()
            var sumX = 0.0
            var sumY = 0.0
            for (match in matches) {
                val srcPoint = keypointsSourceList[match.queryIdx].pt
                sumX += srcPoint.x
                sumY += srcPoint.y
            }
            avgX = sumX / matches.size
            avgY = sumY / matches.size
            //未找到
            if (avgX == 0.0 || avgY == 0.0){
                if (matchSave){
                    return false
                }
            }else{//找到
                if (!matchSave){
                    return false
                }
            }
        }
        //找到继续=true，未找到继续=false
        if (matchSave){
            sai.point = Point(avgX.toFloat(),avgY.toFloat())
        }
        //是否继续后面的命令
        return true
    }

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