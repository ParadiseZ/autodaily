package com.smart.autodaily.handler

import com.smart.autodaily.command.AdbClick
import com.smart.autodaily.command.Operation
import com.smart.autodaily.data.entity.DetectResult
import com.smart.autodaily.data.entity.Point
import com.smart.autodaily.data.entity.ScriptActionInfo
import com.smart.autodaily.utils.Lom
import com.smart.autodaily.utils.getMd5Hash
import com.smart.autodaily.utils.getPicture

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
    // Initialize retry counter
    var retryCount = 0
    while (retryCount < conf.maxRetryNum) {
        // Execute click operation
        when (cmd.operation) {
            is AdbClick -> {
                if (sai.executeMax > 1) {
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
            }
        }
        
        // Wait for response with timeout
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < conf.retryDelay) {
            // Check for response using initial hash
            Thread.sleep(conf.intervalTime) // Check every 500ms
            if (hasResponse()) {
                return 2 // Success
            }
        }
        retryCount++
        Lom.n(INFO, "点击未响应，第${retryCount}次重试")
    }

    // If we get here, all retries failed
    return 4 // Operation failed
}

// Helper function to check for response after click
private fun hasResponse(): Boolean {
    val newCapture = getPicture() ?: return false
    // Compare with initial screenshot
    val newMd5 = getMd5Hash(newCapture)
    if(newMd5.contentEquals(conf.beforeHash)){
        return false
    }
    conf.beforeHash = newMd5
    conf.capture = newCapture
    return true
}

fun checkColor(sai: ScriptActionInfo,ocrRes : Array<DetectResult>) : Boolean{
    if (sai.hsv.isEmpty()){
        return true
    }
    return matchColorAndSetPoints(sai, ocrRes)
}

private fun matchColorAndSetPoints(sai: ScriptActionInfo,ocrRes : Array<DetectResult>) : Boolean{
    val firFilter = ocrRes.filter { it.label == 0 && it.ocr!!.label.containsAll(sai.txtFirstLab) }
    if (firFilter.isEmpty()) {
        Lom.d(ERROR, "颜色匹配失败${sai.id} ${sai.txtLabel}")
        return false
    }
    val secFilter: List<DetectResult>
    if(firFilter.size > 1){
        val minSize = firFilter.minOf { it.ocr!!.label.size }
        secFilter = firFilter.filter {
            it.ocr!!.label.size == minSize
        }
    }else{
        secFilter = firFilter
    }
    //非第一个，排序
    if (sai.labelPos>0){
        secFilter.sortedWith(
            compareBy(
                {it.yCenter} ,{ it.xCenter}
            )
        )
        val idx = (sai.labelPos-1).coerceAtMost(secFilter.size-1)
        secFilter[idx].let {
            Lom.d(INFO, "目标色 ${sai.id}" ,sai.hsv)
            Lom.d(INFO, "图像色 ${sai.id}" ,it.ocr!!.colorSet)
            if (it.ocr.colorSet.containsAll(sai.hsv)){
                sai.point = getPoint(it)
                return true
            }
        }
    }else{
        //默认第一个
        secFilter[0].let {
            Lom.d(INFO, "目标 ${sai.id}" ,sai.hsv)
            Lom.d(INFO, "图像 ${sai.id}" ,it.ocr!!.colorSet)
            if (it.ocr.colorSet.containsAll(sai.hsv)){
                sai.point = getPoint(it)
                return true
            }
        }
    }
    return false
}

fun setPointsByLabel(sai: ScriptActionInfo, detectRes: Array<DetectResult>){
    val firFilter = detectRes.filter {
        it.label == sai.intFirstLab
    }
    if (firFilter.isEmpty()){
        Lom.d(ERROR, "检测点设置异常！${sai.id} ${sai.intLabel}")
        return
    }
    if (sai.labelPos>0){
        firFilter.sortedWith(
            compareBy(
                {it.yCenter} ,{ it.xCenter}
            )
        )
        val idx = sai.labelPos.coerceAtMost(firFilter.size-1)
        firFilter[idx].let {
            sai.point = getPoint(it)
        }
    }else{
        sai.point = getPoint(firFilter[0])
    }
}

fun getPoint(detect : DetectResult) : Point{
    return Point(detect.xCenter + conf.random, detect.yCenter + conf.random)
}
