package com.smart.autodaily.command

import android.graphics.Bitmap
import com.smart.autodaily.handler.ERROR
import com.smart.autodaily.handler.isRunning
import com.smart.autodaily.utils.Lom
import com.smart.autodaily.utils.RootUtil
import com.smart.autodaily.utils.runScope
import kotlinx.coroutines.cancelChildren

class RootExecutor : CommandExecutor {
    override fun execCap(scale: Int) : Bitmap? {
        try {
            return RootUtil.execCap(ShellCommandBuilder.capture(),scale)
        }catch(e : Exception){
            exceptionHandler(e)
        }
        return null
    }
    override fun execVoidCommand(command: String) {
        try {
            RootUtil.execVoidCommand(ShellCommandBuilder.build(command))
        }catch(e : Exception){
            exceptionHandler(e)
        }
    }

    override fun exceptionHandler(e: Exception) {
        runScope.coroutineContext.cancelChildren()
        isRunning.intValue = 0
        Lom.n(ERROR, "Root执行器执行失败，停止运行${e.message}")
    }
}