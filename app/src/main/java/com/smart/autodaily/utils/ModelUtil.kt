package com.smart.autodaily.utils

import com.smart.autodaily.navpkg.AutoDaily
import splitties.init.appCtx

object ModelUtil{
    val model = AutoDaily()
    fun reloadModel(modelPath:String, targetSize : Int, useGpu : Boolean){
        val modelLoaded = model.loadModel(appCtx.assets,modelPath,targetSize,useGpu)
        if (!modelLoaded){
            appCtx.toastOnUi("加载模型失败！")
            println("load model failed")
        }
    }
}
