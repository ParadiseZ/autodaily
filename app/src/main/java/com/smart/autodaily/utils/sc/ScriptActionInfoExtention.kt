package com.smart.autodaily.utils.sc

import com.jeremyliao.liveeventbus.LiveEventBus
import com.smart.autodaily.data.appDb
import com.smart.autodaily.data.entity.Point
import com.smart.autodaily.data.entity.ScriptActionInfo
import com.smart.autodaily.data.entity.ScriptSetInfo
import com.smart.autodaily.handler.ActionHandler
import com.smart.autodaily.utils.ScreenCaptureUtil
import com.smart.autodaily.utils.ShizukuUtil
import com.smart.autodaily.utils.debug


fun ScriptActionInfo.asClick(point: Point? = null) : ScriptActionInfo{
    pointCheck(
        point,
        actionThis = {
            LiveEventBus.get<Point>("click").post(this.point)
        },
        actionPoint = {
            LiveEventBus.get<Point>("click").post(point)
        },
        actionCenter = {
            LiveEventBus.get<Point>("longClick").post(Point((ScreenCaptureUtil.displayMetrics!!.widthPixels/2).toFloat(),(ScreenCaptureUtil.displayMetrics!!.heightPixels/2).toFloat()))
        }
    )
    return this
}

fun ScriptActionInfo.asLongClick(point: Point?= null) : ScriptActionInfo{
    pointCheck(
        point,
        actionThis = {
            LiveEventBus.get<Point>("longClick").post(this.point)
        },
        actionPoint = {
            LiveEventBus.get<Point>("longClick").post(point)
        },
        actionCenter = {
            LiveEventBus.get<Point>("longClick").post(Point((ScreenCaptureUtil.displayMetrics!!.widthPixels/2).toFloat(),(ScreenCaptureUtil.displayMetrics!!.heightPixels/2).toFloat()))
        }
    )
    return this
}

fun ScriptActionInfo.overSet(ssi: ScriptSetInfo, result : Boolean = true, alsoUpdateParent : Boolean = false) : ScriptActionInfo{
    if (alsoUpdateParent){
        appDb!!.scriptSetInfoDao.updateParentAndChildResultFlag(setId, result)
        //更新遍历数据，影响外层while循环[父级ScriptSetInfo]
        ssi.resultFlag = result
    }else{
        appDb!!.scriptSetInfoDao.updateResultFlag(setId, result)
        //只停止自身，scriptActionList.forEach {}停止自身时直接跳过
        this.skipFlag =  true
    }
    return this
}

fun ScriptActionInfo.check(setId : Int) : ScriptActionInfo{
    if( appDb!!.scriptSetInfoDao.getResultFlag(setId)   ){
        this.skipFlag = false
    }else{
        this.skipFlag = true
    }
    return this
}

fun ScriptActionInfo.skip() : ScriptActionInfo{
    this.skipFlag = true
    return this
}

fun ScriptActionInfo.overScript(function : ()->Unit) : ScriptActionInfo{
    function()
    return this
}

fun ScriptActionInfo.runStep(sai: ScriptActionInfo) : ScriptActionInfo{

    return this
}

fun ScriptActionInfo.adbClick(point: Point?= null) : ScriptActionInfo{
    pointCheck(
        point,
        actionThis = {
            ShizukuUtil.iUserService?.execLine("shell input tap ${this.point!!.x + ActionHandler.randomXRange} ${this.point!!.y + ActionHandler.randomYRange}")
        },
        actionPoint = {
            ShizukuUtil.iUserService?.execLine("shell input tap ${point!!.x + ActionHandler.randomXRange} ${point.y + ActionHandler.randomYRange}")
        },
        actionCenter = {
            ShizukuUtil.iUserService?.execLine("shell input tap ${ScreenCaptureUtil.displayMetrics!!.widthPixels / 2 + ActionHandler.randomXRange} ${ScreenCaptureUtil.displayMetrics!!.heightPixels/2 + ActionHandler.randomYRange}")
        }
    )
    return this
}

fun ScriptActionInfo.adbSwap(point: Point?= null) : ScriptActionInfo{
    pointCheck(
        point,
        actionThis = {
            ShizukuUtil.iUserService?.execLine("shell input swipe ${this.point!!.x + ActionHandler.randomXRange} ${this.point!!.y + ActionHandler.randomYRange}")
        },
        actionPoint = {
            ShizukuUtil.iUserService?.execLine("shell input swipe ${point!!.x + ActionHandler.randomXRange} ${point.y + ActionHandler.randomYRange}")
        },
        actionCenter = {
            ShizukuUtil.iUserService?.execLine("shell input swipe ${ScreenCaptureUtil.displayMetrics!!.widthPixels / 2 + ActionHandler.randomXRange} ${ScreenCaptureUtil.displayMetrics!!.heightPixels/2 + ActionHandler.randomYRange}")
        }
    )
    return this
}

fun ScriptActionInfo.sleep(time: Long) : ScriptActionInfo{
    Thread.sleep(time)
    return this
}
private fun ScriptActionInfo.pointCheck(point: Point? = null , actionThis: () -> Unit, actionPoint: () -> Unit, actionCenter:()-> Unit){
    if (point == null){
        if(this.point !=null ){
            debug("click point : ${this.point}")
            actionThis()
        }else{
            debug("click point : ${ScreenCaptureUtil.displayMetrics!!.widthPixels / 2 + ActionHandler.randomXRange},${ScreenCaptureUtil.displayMetrics!!.heightPixels/2 + ActionHandler.randomYRange}")
            actionCenter()
        }
    }else{
        debug("click point : $point")
        actionPoint()
    }
}