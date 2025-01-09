package com.smart.autodaily.handler

import com.smart.autodaily.command.AdbClick
import com.smart.autodaily.command.Operation
import com.smart.autodaily.data.entity.DetectResult
import com.smart.autodaily.data.entity.OcrResult
import com.smart.autodaily.data.entity.ScriptActionInfo
import com.smart.autodaily.handler.RunScript.getPicture
import com.smart.autodaily.handler.RunScript.setPoints
import com.smart.autodaily.utils.cancelChildrenJob
import com.smart.autodaily.utils.getMd5Hash

fun execClick(sai: ScriptActionInfo, detectRes: Array<DetectResult>, ocrRes : Array<OcrResult>, cmd : Operation) : Int {
    setPoints(sai, detectRes, ocrRes)
    println("执行$sai")
    if (sai.point == null) {
        println("点击位置为空，错误！")
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
                    if (sai.executeCur >= sai.executeMax) {
                        sai.executeCur = 0
                        sai.skipFlag = true
                    }
                }
                cmd.exec(sai)
            }
        }
        
        // Wait for response with timeout
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < conf.retryDelay) {
            // Check for response using initial hash
            if (hasResponse()) {
                return 2 // Success
            }
            Thread.sleep(conf.intervalTime) // Check every 500ms
        }

        retryCount++
        println("点击未响应，第${retryCount}次重试")
    }

    // If we get here, all retries failed
    println("点击操作失败，已达到最大重试次数")
    
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
        cancelChildrenJob()
        return false
    }
}
