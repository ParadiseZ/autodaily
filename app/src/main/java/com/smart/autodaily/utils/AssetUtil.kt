package com.smart.autodaily.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import splitties.init.appCtx
import java.io.IOException
import java.io.InputStream


object AssetUtil {
    fun getFromAssets(fileName: String): Bitmap{
        var inputStream: InputStream? = null
        try {
            // 打开 assets 文件夹中的文件输入流
            inputStream = appCtx.assets.open(fileName)
            // 使用 BitmapFactory 解码输入流为 Bitmap
            return BitmapFactory.decodeStream(inputStream)
        } finally {
            // 确保在任何情况下都关闭输入流
            if (inputStream != null) {
                try {
                    inputStream.close()
                } catch (e: IOException) {
                    // 忽略关闭流时发生的 IO 错误
                }
            }
        }
    }
}