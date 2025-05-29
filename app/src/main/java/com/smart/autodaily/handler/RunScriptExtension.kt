package com.smart.autodaily.handler

import com.smart.autodaily.command.Operation
import com.smart.autodaily.data.entity.DetectResult
import com.smart.autodaily.data.entity.Point
import com.smart.autodaily.data.entity.ScriptActionInfo
import com.smart.autodaily.utils.Lom
import kotlin.math.roundToInt

fun execClick(sai: ScriptActionInfo, detectRes: Array<DetectResult>, cmd : Operation) : Int {
    if (!sai.operTxt){
        sai.point=null
        setPointsByLabel(sai, detectRes)
    }else if(sai.hsv.isEmpty()){
        matchColorAndSetPoints(sai,detectRes)
    }
    if (sai.point == null) {
        return 5
    }
    if (sai.executeMax > 0) {
        sai.executeCur += 1
        Lom.d(INFO, "准备第${sai.executeCur}次点击")
        if (sai.executeCur >= sai.executeMax) {
            Lom.d(INFO, "已达到最大点击次数，设置跳过")
            sai.executeCur = 0
            sai.skipFlag = true
        }
    }
    Lom.d(INFO, "点击${sai.point}")
    cmd.exec(sai)
    return 2 // Operation failed 4
}

fun checkColor(sai: ScriptActionInfo,ocrRes : Array<DetectResult>) : Boolean{
    if (sai.hsv.isEmpty()){
        return true
    }
    return matchColorAndSetPoints(sai, ocrRes)
}

private fun matchColorAndSetPoints(sai: ScriptActionInfo,ocrRes : Array<DetectResult>) : Boolean{
    var firFilter = ocrRes.filter { item ->
        if (item.ocr == null) {
            false
        } else {
            val labelMatch = item.ocr.label.containsAll(sai.txtFirstLab)
            val colorMatch = sai.hsv.isEmpty() || item.ocr.colorSet.containsAll(sai.hsv)
            labelMatch && colorMatch
        }
    }
    if (firFilter.isEmpty()) {
        Lom.d(ERROR, "saiId:${sai.id},颜色匹配,目标${sai.hsv}")
        return false
    }
    //val secFilter: List<DetectResult>
    if(firFilter.size > 1) {
        val minSize = firFilter.minOf { it.ocr!!.label.size }
        firFilter = firFilter.filter {
            it.ocr!!.label.size == minSize
        }
    }
    //非第一个，排序
    if (sai.labelPos>0 && firFilter.size > 1){
        val errorValue = if(conf.capScale == 1){20} else 10
        firFilter = firFilter.sortedWith(
            compareBy(
                { (it.yCenter / errorValue).roundToInt()} ,{ (it.xCenter / errorValue).roundToInt() }
            )
        )
        val idx = (sai.labelPos-1).coerceAtMost(firFilter.size-1)
        firFilter[idx].let {
            Lom.d(INFO, "i ${idx},num ${firFilter.size},saiId ${sai.id},目标色${sai.hsv},图像色${it.ocr!!.colorSet}")
            if (it.ocr.colorSet.containsAll(sai.hsv)){
                sai.point = getPoint(it)
                return true
            }
        }
    }else{
        //默认第一个
        firFilter[0].let {
            Lom.d(INFO, "saiId ${sai.id},目标色${sai.hsv},图像色${it.ocr!!.colorSet}")
            if (it.ocr.colorSet.containsAll(sai.hsv)){
                sai.point = getPoint(it)
                return true
            }
        }
    }
    return false
}

fun setPointsByLabel(sai: ScriptActionInfo, detectRes: Array<DetectResult>){
    var firFilter = detectRes.filter {
        it.label == sai.intFirstLab
    }
    if (firFilter.isEmpty()){
        Lom.d(ERROR, "检测点设置异常！${sai.id} ${sai.intLabel}")
        return
    }
    if (sai.labelPos>0){
        val errorValue = if(conf.capScale == 1){20} else 10
        firFilter = firFilter.sortedWith(
            compareBy(
                { (it.yCenter / errorValue).roundToInt()} ,{ (it.xCenter / errorValue).roundToInt() }
            )
        )
        val idx = (sai.labelPos-1).coerceAtMost(firFilter.size-1)
        Lom.d(INFO, "i ${idx},num ${firFilter.size},saiId ${sai.id}")
        firFilter[idx].let {
            sai.point = getPoint(it)
        }
    }else{
        sai.point = getPoint(firFilter[0])
    }
}

fun getPoint(detect : DetectResult) : Point{
    return Point((detect.xCenter * conf.capScale + conf.random).toInt(), (detect.yCenter * conf.capScale  + conf.random).toInt())
}
