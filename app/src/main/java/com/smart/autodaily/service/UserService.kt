package com.smart.autodaily.service

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.smart.autodaily.IUserService
import com.smart.autodaily.utils.BitmapPool
import com.smart.autodaily.utils.ScreenCaptureUtil
import splitties.init.appCtx
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.system.exitProcess


class UserService: IUserService.Stub(){
    override fun destroy() {
        exitProcess(0)
    }

    override fun exit() {
        destroy()
    }

    override fun execLine(command: String?): String? {
        try {
            return readResult(Runtime.getRuntime().exec(command))
        }catch (e : Exception){
            println("execLine error:${e.message}")
            return null
        }
    }

    override fun execVoidComand(command: String)  {
        try {
            Runtime.getRuntime().exec(command)
        }catch (e : Exception){
            println("execVoidComand error:${e.message}")
        }
    }

    override fun execArr(command: Array<out String>?): String? {
        return  execLine(command.toString())
    }

    override fun execCap(command:String?,scale : Int): Bitmap {
        return readBitmap(Runtime.getRuntime().exec(command),scale)
    }

    fun readBitmap(process: Process, scale: Int): Bitmap {
        val screen = ScreenCaptureUtil.getDisplayMetrics(appCtx)
        val targetWidth = screen.widthPixels / scale
        val targetHeight = screen.heightPixels / scale

        val inputStream = process.inputStream
        var acquiredBitmapFromPool: Bitmap? = null
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = false
            inSampleSize = scale // BitmapFactory handles the scaling based on this sample size
        }

        try {
            acquiredBitmapFromPool = BitmapPool.acquire(targetWidth, targetHeight)
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

            // Decoding succeeded, decodedBitmap is not null
            if (acquiredBitmapFromPool != null && decodedBitmap !== acquiredBitmapFromPool) {
                // A bitmap was acquired from the pool, but BitmapFactory created a new one.
                // Recycle the acquired one as it was not used.
                BitmapPool.recycle(acquiredBitmapFromPool)
                acquiredBitmapFromPool = null // Mark as handled
            }
            // If acquiredBitmapFromPool was successfully reused (decodedBitmap === acquiredBitmapFromPool), it's returned.
            // If acquiredBitmapFromPool was null, decodedBitmap is a newly created bitmap.
            return decodedBitmap

        } catch (e: Exception) {
            // An exception occurred during the process (e.g., from acquire, decodeStream, or explicit throw)
            if (acquiredBitmapFromPool != null) {
                // If a bitmap was acquired from the pool and an error occurred before it was
                // properly handled (returned or recycled), recycle it here to prevent leaks.
                BitmapPool.recycle(acquiredBitmapFromPool)
            }
            println("Error in readBitmap: ${e.message}") // Log the error
            throw e // Re-throw the exception to be handled by the caller
        } finally {
            try {
                inputStream.close() // Always try to close the input stream
            } catch (ioe: java.io.IOException) {
                println("Error closing input stream in readBitmap: ${ioe.message}")
            }
            try {
                process.waitFor() // Wait for the native process to complete, similar to readResult
            } catch (ie: InterruptedException) {
                Thread.currentThread().interrupt() // Restore interruption status
                println("Process wait interrupted in readBitmap: ${ie.message}")
            } catch (e: Exception) {
                println("Error waiting for process in readBitmap: ${e.message}")
            }
        }
    }

    fun readResult(process: Process): String {
        try {
            val stringBuilder = StringBuilder()
            // 读取执行结果
            val inputStreamReader = InputStreamReader(process.inputStream)
            val bufferedReader = BufferedReader(inputStreamReader)
            var line: String?
            while ((bufferedReader.readLine().also { line = it }) != null) {
                stringBuilder.append(line).append("\n")
            }
            inputStreamReader.close()
            process.waitFor()
            return stringBuilder.toString()
        }catch (_ : Exception){
            return ""
        }

    }
}