package com.smart.autodaily.navpkg

import android.content.res.AssetManager
import android.graphics.Bitmap
import com.smart.autodaily.data.entity.DetectResult

class AutoDaily{
    external fun loadModel(mgr : AssetManager,modelPath : String, targetSize: Int, useGpu : Boolean) : Boolean
    external fun detect(capture: Bitmap,numClasses:Int, threshold: Float=0.4f, nmsThreshold: Float=0.5f) : Array<DetectResult>

    external fun detectAndDraw(capture: Bitmap,numClasses:Int, threshold: Float=0.4f, nmsThreshold: Float=0.5f, drawMap: Bitmap) : Array<DetectResult>

    companion object{
        init {
            System.loadLibrary("autodaily")
        }
    }
}