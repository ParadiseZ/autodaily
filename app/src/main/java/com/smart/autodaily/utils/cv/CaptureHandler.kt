package com.smart.autodaily.utils.cv

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.Mat

object CaptureHandler {

    fun saveCaptureMat(bitmap: Bitmap, mat: Mat){
        Utils.bitmapToMat(bitmap, mat)
    }
}