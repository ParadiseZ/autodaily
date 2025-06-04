package com.smart.autodaily.utils

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import java.io.File
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
fun getMd5(file : File):ByteArray{
    file.forEachBlock { bytes, size ->
        md.update(bytes, 0, size)
    }
    return md.digest()
}
fun getMd5Str(file : File) : String{
    val res = getMd5(file)
    val str = StringBuilder()
    for (b in res) {
        str.append(String.format("%02x", b))
    }
    return str.toString()
}