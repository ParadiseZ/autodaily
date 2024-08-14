package com.smart.autodaily.navpkg

import android.content.res.AssetManager
import android.graphics.Bitmap
import com.smart.autodaily.data.entity.DetectResult

class AutoDaily{
    external fun loadModel(mgr : AssetManager,modelPath : String, targetSize: Int, useGpu : Boolean) : Boolean
    external fun detect(capture: Bitmap, threshold: Float, nmsThreshold: Float) : Array<DetectResult>

    companion object{
        init {
            System.loadLibrary("autodaily")
        }
    }
}