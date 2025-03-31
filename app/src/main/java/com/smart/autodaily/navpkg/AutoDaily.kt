package com.smart.autodaily.navpkg

import android.content.res.AssetManager
import android.graphics.Bitmap
import com.smart.autodaily.data.entity.DetectResult

class AutoDaily{
    external fun loadModelSec(mgr : AssetManager,paramPath : String, modelPath : String, targetSize: Int, useGpu : Boolean,colorStep : Short,lang : Int, ocr : Boolean, getColor: Boolean) : Boolean
    external fun detectYolo(capture: Bitmap,numClasses:Int, threshold: Float = 0.25f, nmsThreshold: Float = 0.45f) : Array<DetectResult>
    external fun hsvToColor(h : Short, s : Short, v : Short) : Int
    companion object{
        init {
            System.loadLibrary("autodaily")
        }
    }
}