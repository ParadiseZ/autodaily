package com.smart.autodaily.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.os.SystemClock
import android.util.DisplayMetrics

object ScreenCaptureUtil {
    val mediaProjectionDataMap = mutableMapOf<String, Any>()//MainActivity中初始化
    var mps: MediaProjection? = null//MainActivity中初始化
    var displayMetrics: DisplayMetrics? = null//MainActivity中初始化
    private var imgReader: ImageReader? = null
    private var virtualDisplay: VirtualDisplay? = null

    fun getDisplayMetrics(context: Context) : DisplayMetrics? {
        return context.resources.displayMetrics
    }

    fun screenCapture(): Image? {
        try {
            displayMetrics?.let { dms ->
                imgReader?:let {
                    imgReader = ImageReader.newInstance(
                        dms.widthPixels, dms.heightPixels,
                        PixelFormat.RGBA_8888, 2)
                }
                virtualDisplay?:let {
                    virtualDisplay = mps?.createVirtualDisplay("MediaProjection", dms.widthPixels, dms.heightPixels, dms.densityDpi,
                        DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                        imgReader?.surface, null, null)
                }
            }
            //return imgToBitmap(image)
        }catch (e: Exception){
            println("screenCapture error: "+e.message)
        }
        SystemClock.sleep(1000)
        val  image = imgReader?.acquireLatestImage()
        virtualDisplay?.release()
        return image
    }

    fun imgToBitmap(image: Image) : Bitmap {
        val width = image.width
        val height = image.height
        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val rowPadding = rowStride - pixelStride * width
        val bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(buffer)
        return bitmap
    }

    fun release() {
        mps?.stop()
        imgReader?.close()
        virtualDisplay?.release()
        mediaProjectionDataMap.clear()
        displayMetrics=null
    }
}