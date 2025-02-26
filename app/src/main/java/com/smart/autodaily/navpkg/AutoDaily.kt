package com.smart.autodaily.navpkg

import android.content.res.AssetManager
import android.graphics.Bitmap
import com.smart.autodaily.data.entity.DetectResult
import com.smart.autodaily.data.entity.OcrResult

class AutoDaily{
    //external fun loadModel(mgr : AssetManager,modelPath : String, targetSize: Int, useGpu : Boolean) : Boolean
    external fun loadModelSec(paramPath : String, modelPath : String, targetSize: Int, useGpu : Boolean) : Boolean
    external fun detectYolo(capture: Bitmap,numClasses:Int, threshold: Float = 0.25f, nmsThreshold: Float = 0.45f) : Array<DetectResult>
    //external fun detectAndDraw(capture: Bitmap,numClasses:Int, threshold: Float=0.25f, nmsThreshold: Float=0.5f, drawMap: Bitmap) : Array<DetectResult>

    external fun loadOcr(mgr : AssetManager, lang : Int, useGpu: Boolean, detectSize: Int, colorNum : Short, colorStep : Short) :Boolean
    external fun detectOcr(capture: Bitmap) : Array<OcrResult>

    external fun hsvToColor(h : Short, s : Short, v : Short) : Int

    external fun frameDiff(beforeBitmap: Bitmap, afterBitmap: Bitmap, targetSize: Int, range: Int) : Float
    companion object{
        init {
            System.loadLibrary("autodaily")
        }
    }
}