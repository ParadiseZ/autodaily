package com.smart.autodaily.utils

import com.smart.autodaily.navpkg.AutoDaily
import splitties.init.appCtx

object ModelUtil{
    val model = AutoDaily()
    fun reloadModel(modelPath:String, targetSize : Int, useGpu : Boolean){
        val modelLoaded = model.loadModel(appCtx.assets,modelPath,targetSize,useGpu)
        if (!modelLoaded){
            appCtx.toastOnUi("加载模型失败！")
        }
    }

    fun reloadModelSec(paramPath: String, modelPath:String, targetSize : Int, useGpu : Boolean){
        if (!model.loadModelSec(paramPath,modelPath,targetSize,useGpu)){
            appCtx.toastOnUi("加载模型失败！")
        }
    }

    fun loadOcr(lang : Int, useGpu: Boolean){
        if (!model.loadOcr(appCtx.assets, lang, useGpu)){
            appCtx.toastOnUi("加载OCR模型失败！")
        }
    }
}
