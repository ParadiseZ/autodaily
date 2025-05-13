package com.smart.autodaily.utils

import android.graphics.Bitmap
import com.smart.autodaily.command.CAPTURE
import com.smart.autodaily.handler.ERROR
import com.smart.autodaily.handler.isRunning
import kotlinx.coroutines.cancelChildren

fun getPicture() : Bitmap?{
    try {
        ShizukuUtil.iUserService?.let {
            return it.execCap(CAPTURE)
        }
        /*appCtx.assets.open("0184.jpg").use { inputStream ->
            return BitmapFactory.decodeStream(inputStream)
        }*/
        return null
    }catch (_:Exception){
        runScope.coroutineContext.cancelChildren()
        isRunning.intValue = 0
        Lom.n(ERROR, "截图失败，停止运行")
        return null
    }
}