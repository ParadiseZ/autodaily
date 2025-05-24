package com.smart.autodaily.command

import android.graphics.Bitmap
import com.smart.autodaily.handler.ERROR
import com.smart.autodaily.handler.isRunning
import com.smart.autodaily.utils.Lom
import com.smart.autodaily.utils.ScreenCaptureUtil
import com.smart.autodaily.utils.ShizukuUtil
import com.smart.autodaily.utils.runScope
import kotlinx.coroutines.cancelChildren
import splitties.init.appCtx

class ShizukuExecutor : CommandExecutor {
    override fun execCap(scale: Int) : Bitmap? {
        val displayMetrics = ScreenCaptureUtil.getDisplayMetrics(appCtx)
        try {
            return ShizukuUtil.iUserService?.execCap(ShellCommandBuilder.capture(),displayMetrics.widthPixels,displayMetrics.heightPixels,scale)
        } catch(e : Exception) {
            exceptionHandler(e)
        }
        return null
    }

    override fun execVoidCommand(command: String) {
        try {
            ShizukuUtil.iUserService?.execVoidComand(ShellCommandBuilder.build(command))
        }catch(e : Exception){
            exceptionHandler(e)
        }
    }

    override fun exceptionHandler(e: Exception) {
        runScope.coroutineContext.cancelChildren()
        isRunning.intValue = 0
        Lom.n(ERROR, "Shizuku执行器执行失败，停止运行${e.message}")
        e.printStackTrace()
    }
}