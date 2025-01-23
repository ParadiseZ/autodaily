package com.smart.autodaily.handler

import com.smart.autodaily.command.AdbClick
import com.smart.autodaily.command.Operation
import com.smart.autodaily.data.entity.DetectResult
import com.smart.autodaily.data.entity.OcrResult
import com.smart.autodaily.data.entity.ScriptActionInfo
import com.smart.autodaily.handler.RunScript.getPicture
import com.smart.autodaily.handler.RunScript.setPoints
import com.smart.autodaily.utils.Lom
import com.smart.autodaily.utils.getMd5Hash
import com.smart.autodaily.utils.runScope
import kotlinx.coroutines.cancelChildren

fun execClick(sai: ScriptActionInfo, detectRes: Array<DetectResult>, ocrRes : Array<OcrResult>, cmd : Operation) : Int {
    setPoints(sai, detectRes, ocrRes)
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
    try {
        val newCapture = getPicture() ?: return false
        // Compare with initial screenshot
        val newMd5 = getMd5Hash(newCapture)
        if(newMd5.contentEquals(conf.beforeHash)){
            return false
        }
        conf.beforeHash = newMd5
        conf.capture = newCapture
        return true
    } catch (e: Exception) {
        runScope.coroutineContext.cancelChildren()
        Lom.n(ERROR, "截图失败，停止运行")
        return false
    }
}
