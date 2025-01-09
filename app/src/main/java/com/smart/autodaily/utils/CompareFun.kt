package com.smart.autodaily.utils

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import java.security.MessageDigest

val md by lazy {
    MessageDigest.getInstance("MD5")
}
fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    return stream.toByteArray()
}
fun getMd5Hash(bitmap: Bitmap): ByteArray {
    return md.digest(
        bitmapToByteArray(bitmap)
    )
}