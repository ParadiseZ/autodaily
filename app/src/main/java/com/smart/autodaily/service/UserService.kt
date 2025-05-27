package com.smart.autodaily.service

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.smart.autodaily.IUserService
import com.smart.autodaily.utils.BitmapPool
import kotlin.system.exitProcess


class UserService: IUserService.Stub(){
    override fun destroy() {
        exitProcess(0)
    }

    override fun exit() {
        destroy()
    }
    override fun execVoidComand(command: String)  {
        Runtime.getRuntime().exec(command)
    }

    override fun execCap(command:String?,width:Int, height:Int, scale:Int): Bitmap {
        val process = Runtime.getRuntime().exec(command)
        val inputStream = process.inputStream
        var acquiredBitmapFromPool: Bitmap? = null
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = false
            inSampleSize = scale // BitmapFactory handles the scaling based on this sample size
        }
        try {
            acquiredBitmapFromPool = BitmapPool.acquire(width, height)
            if (acquiredBitmapFromPool != null) {
                options.inBitmap = acquiredBitmapFromPool // Attempt to reuse the acquired bitmap
            }
            val decodedBitmap = BitmapFactory.decodeStream(inputStream, null, options)
            if (decodedBitmap == null) {
                // Decoding failed
                if (acquiredBitmapFromPool != null) {
                    BitmapPool.recycle(acquiredBitmapFromPool)
                    acquiredBitmapFromPool = null // Mark as handled to prevent double recycling in catch block
                }
                throw IllegalStateException("Failed to decode bitmap from process stream.")
            }

            if (acquiredBitmapFromPool != null && decodedBitmap !== acquiredBitmapFromPool) {
                BitmapPool.recycle(acquiredBitmapFromPool)
                acquiredBitmapFromPool = null // Mark as handled
            }
            return decodedBitmap

        } catch (e: Exception) {
            if (acquiredBitmapFromPool != null) {
                BitmapPool.recycle(acquiredBitmapFromPool)
            }
            throw e
        } finally {
            inputStream.close()
            process.waitFor()
        }
    }
}