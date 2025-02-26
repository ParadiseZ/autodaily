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
        }?:{throw IllegalStateException()}
        return null
    }catch (e:Exception){
        runScope.coroutineContext.cancelChildren()
        isRunning.intValue = 0
        Lom.n(ERROR, "截图失败，停止运行")
        return null
    }
}

fun isSame(threshold : Float,targetSize: Int = 640, range:Int =10) : Boolean{
    val before = getPicture()?: return false
    Thread.sleep(500)
    val cur = getPicture()?: return false
    if (ModelUtil.model.frameDiff(before, cur , targetSize, range)< threshold) {
        return true
    }
    return false
}