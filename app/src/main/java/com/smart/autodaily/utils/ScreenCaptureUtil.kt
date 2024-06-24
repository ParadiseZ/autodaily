package com.smart.autodaily.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.os.Build
import android.os.Environment
import android.util.DisplayMetrics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import splitties.init.appCtx
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ScreenCaptureUtil : CoroutineScope by MainScope(){
    const val MAX_IMAGE_NUM =  3//缓存图片最大数量
    private var currentImageNum = 1//当前缓存图片数量
    val mediaProjectionDataMap = mutableMapOf<String, Any>()//MainActivity中初始化
    var mps: MediaProjection? = null//MediaProjectionService服务开启命令时获取
    var displayMetrics: DisplayMetrics? = null//MainActivity中允许权限时获取
    var imgReader: ImageReader? = null//截图时获取，超过MAX_IMAGE_NUM释放再获取
    var virtualDisplay: VirtualDisplay? = null//截图时获取，超过MAX_IMAGE_NUM释放再获取

    fun getDisplayMetrics(context: Context) : DisplayMetrics? {
        return context.resources.displayMetrics
    }

    fun setDisplayMetrics(context: Context){
        displayMetrics = getDisplayMetrics(context)
    }


    fun screenCaptureTIRAMISU() {
        if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.TIRAMISU){

        }
    }
    fun screenCapture(): Bitmap? {
        try {
            displayMetrics?.let { dms ->
                imgReader?:let {
                    imgReader = ImageReader.newInstance(
                        dms.widthPixels, dms.heightPixels,
                        PixelFormat.RGBA_8888, MAX_IMAGE_NUM)
                }
                virtualDisplay?:let {
                    virtualDisplay = mps?.createVirtualDisplay("MediaProjection", dms.widthPixels, dms.heightPixels, dms.densityDpi,
                        DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                        imgReader?.surface, null, null)
                }
            }
        }catch (e: Exception){
            println("screenCapture error: "+e.message)
            releaseCapture()
            currentImageNum = 1
        }
        val  image = imgReader?.acquireLatestImage()
        return image?.let {
            currentImageNum++
            if (currentImageNum == MAX_IMAGE_NUM) {
                println("释放图片资源")
                currentImageNum = 1
                releaseCapture()
                return null
            }else{
                //releaseCapture()
                imgToBitmap(it)
            }
        }
    }

    private fun imgToBitmap(image: Image) : Bitmap {
        val width = image.width
        val height = image.height
        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val rowPadding = rowStride - pixelStride * width
        val bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(buffer)
        //saveScreenCapture(bitmap)
        image.close()
        return bitmap
    }

    fun saveScreenCapture(bitmap: Bitmap) {
        /*val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        contentValues.put(MediaStore.Images.Media.TITLE, "screenshot")
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, "screenshot${currentImageNum}.png")
        contentValues.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
        contentValues.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "screenshot")
        contentValues.put(MediaStore.Images.Media.IS_PENDING, 1)
        contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/screenshot")
        val resolver = mediaProjectionDataMap["resolver"] as ContentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        val outputStream = resolver.openOutputStream(uri!!)
        if (outputStream != null) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        }
        outputStream?.close()
        contentValues.clear()
        contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, contentValues, null, null)
        bitmap.recycle()*/

        val filename = "screenshot_${System.currentTimeMillis()}.png"
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), filename)

        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(file)
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.R){
                bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSLESS, 0, out)
            }else{
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            out.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                out?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun release() {
        mps?.stop()
        mps = null
        imgReader?.close()
        imgReader = null
        virtualDisplay?.release()
        virtualDisplay = null
        displayMetrics=null
    }

    fun releaseCapture() {
        imgReader?.close()
        imgReader = null
        virtualDisplay?.release()
        virtualDisplay = null
        setDisplayMetrics(appCtx)
    }

    fun resetScreenSize(width: Int, height: Int){
        releaseCapture()
        displayMetrics?.widthPixels = width
        displayMetrics?.heightPixels = height
    }
}
