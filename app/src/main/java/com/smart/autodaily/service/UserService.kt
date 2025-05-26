package com.smart.autodaily.service

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.smart.autodaily.IUserService
import com.smart.autodaily.command.ShellConstants
import com.smart.autodaily.handler.INFO
import com.smart.autodaily.utils.BitmapPool
import com.smart.autodaily.utils.Lom
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStreamWriter
import java.io.PipedInputStream
import java.io.PipedOutputStream
import kotlin.system.exitProcess


class UserService: IUserService.Stub(){
    private var process: Process? = null
    private var outputStream: OutputStreamWriter? = null
    private var inputStream: InputStream? = null

    private val maxAttempt = 5
    private val maxTryNum = 20

    val END_MARKER_BYTES = "${ShellConstants.END_COMMAND}\n".toByteArray(Charsets.ISO_8859_1)
    val pngHead =  byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)


    override fun destroy() {
        close()
        exitProcess(0)
    }

    override fun exit() {
        destroy()
    }
    override fun execVoidComand(command: String)  {
        if(process == null){
            start(command)
        }else{
            outputStream?:throw Exception("outputStream is null")
            outputStream?.write(command)
            outputStream?.flush()
        }
        tryClearStaleDataFrom(inputStream)
    }

    override fun execCap(command:String,width:Int, height:Int, scale:Int): Bitmap {
        if (outputStream == null || inputStream == null) {
            start(command)
            if (outputStream == null || inputStream == null){
                throw IllegalStateException("shizuku process error")
            }else{
                var acquiredBitmapFromPool : Bitmap?=null
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = false
                    inSampleSize = scale
                }
                try {
                    acquiredBitmapFromPool = BitmapPool.acquire(width, height)
                    if (acquiredBitmapFromPool != null) {
                        options.inBitmap = acquiredBitmapFromPool
                    }
                    val decodedBitmap = BitmapFactory.decodeStream(inputStream, null, options)

                    if (decodedBitmap == null) {
                        throw IllegalStateException("BitmapFactory.decodeByteArray returned null after successful data capture.")
                    }

                    if (acquiredBitmapFromPool != null && decodedBitmap !== acquiredBitmapFromPool) {
                        BitmapPool.recycle(acquiredBitmapFromPool) // Recycle if a new bitmap was decoded
                    }
                    return decodedBitmap
                } catch (e: Exception) {
                    BitmapPool.recycle(acquiredBitmapFromPool) // Ensure recycle on any exception during decoding
                    throw e // Re-throw
                }
            }
        }
        var finalImageData: ByteArray? = null
        var attempt = 0
        while (attempt < maxTryNum) {
            attempt++
            tryClearStaleDataFrom(inputStream)

            val currentImageData = getCapInputStream(command).readBytes()

            var isValidAttempt = false
            if (currentImageData.isNotEmpty()) {
                // Only log extensive details if it's NOT a valid attempt, to reduce noise on success
                val isValidPngHeader = currentImageData.size >= 8 &&
                        currentImageData.take(8).toByteArray().contentEquals(
                            pngHead
                        )

                if (isValidPngHeader && currentImageData.size > 1000) {
                    isValidAttempt = true
                }
            }
            if (isValidAttempt) {
                finalImageData = currentImageData
                break
            }
        }

        if (finalImageData == null) {
            throw IllegalStateException("Failed to capture screen after $maxTryNum attempts")
        }
        var acquiredBitmapFromPool: Bitmap? = null
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = false
            inSampleSize = scale
        }
        try {
            acquiredBitmapFromPool = BitmapPool.acquire(width, height)
            if (acquiredBitmapFromPool != null) {
                options.inBitmap = acquiredBitmapFromPool
            }
            val decodedBitmap = BitmapFactory.decodeByteArray(finalImageData, 0, finalImageData.size, options)

            if (decodedBitmap == null) {
                throw IllegalStateException("BitmapFactory.decodeByteArray returned null after successful data capture.")
            }

            if (acquiredBitmapFromPool != null && decodedBitmap !== acquiredBitmapFromPool) {
                BitmapPool.recycle(acquiredBitmapFromPool) // Recycle if a new bitmap was decoded
            }
            return decodedBitmap

        } catch (e: Exception) {
            BitmapPool.recycle(acquiredBitmapFromPool) // Ensure recycle on any exception during decoding
            throw e // Re-throw
        }
    }

    override fun close(){
        try {
            outputStream?.write("exit\n")
            outputStream?.flush()
        } catch (_: IOException) { /* Ignore */ }
        try {
            outputStream?.close()
        } catch (_: IOException) { /* Ignore */ }
        try {
            inputStream?.close()
        } catch (_: IOException) { /* Ignore */ }
        process?.destroy()
        process = null
        outputStream = null
        inputStream = null
        Lom.d(INFO, "Shizuku process and streams closed.")
    }

    fun start(command: String) {
        if (process == null) {
            try {
                process = Runtime.getRuntime().exec(command)
                inputStream = process?.inputStream
                outputStream = OutputStreamWriter(process?.outputStream, Charsets.UTF_8)
            } catch (e: Exception) {
                process = null
                inputStream = null
                outputStream = null
                throw e
            }
        }
    }

    private fun tryClearStaleDataFrom(stream: InputStream?) {
        if (stream == null) return
        val tempBuffer = ByteArray(128)
        var readAttempt = 0
        try {
            while (stream.available() > 0) {
                readAttempt++
                if (stream.read(tempBuffer) > 0) {
                    continue
                } else {
                    break
                }
                if(readAttempt > maxAttempt){
                    break
                }
            }
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        } catch (e: Exception) {
            throw e
        }
    }
    private fun getCapInputStream(command: String): InputStream{
        val pipedInputStream = PipedInputStream()
        val pipeOut = PipedOutputStream(pipedInputStream)
        val searchBuffer = ByteArrayOutputStream()

        Thread {
            var threadSuccess = false
            var markerFound = false
            try {
                //Lom.n(INFO, "[getCapInputStream] Thread started. Writing command to su: $command")
                outputStream?.write(command)
                outputStream?.flush()
                //Lom.n(INFO, "[getCapInputStream] Command flushed to su process.")

                val buffer = ByteArray(8192)
                var bytesRead: Int

                while (true) {
                    bytesRead = inputStream?.read(buffer) ?: -1
                    //Lom.n(INFO, "[getCapInputStream] Read $bytesRead bytes from su inputStream.")

                    if (bytesRead == -1) {
                        //Lom.n(INFO, "[getCapInputStream] EOF reached on su inputStream. searchBuffer contains ${searchBuffer.size()} bytes.")
                        // Marker not found and EOF reached, any data in searchBuffer is considered the stream's content (or part of it)
                        // This branch will be hit if screencap output does not end with the expected marker before EOF.
                        break // Exit loop, finally block will handle writing remaining buffer if necessary
                    }

                    searchBuffer.write(buffer, 0, bytesRead)
                    //Lom.n(INFO, "[getCapInputStream] Wrote $bytesRead bytes to searchBuffer. Total size: ${searchBuffer.size()}.")
                    val currentData = searchBuffer.toByteArray()
                    // Log first 16 bytes of currentData for debugging if it's reasonably small, or just a part of it
                    // val previewBytes = currentData.take(32).toByteArray()
                    // Lom.n(INFO, "[getCapInputStream] Current searchBuffer data (first 32 bytes hex): ${previewBytes.joinToString("") { "%02x".format(it) }}")

                    val markerPos = indexOfByteArray(currentData, END_MARKER_BYTES)

                    if (markerPos != -1) {
                        //Lom.n(INFO, "[getCapInputStream] END_MARKER found at position $markerPos in searchBuffer (size ${currentData.size}).")
                        val dataToWrite = currentData.copyOfRange(0, markerPos)
                        //Lom.n(INFO, "[getCapInputStream] Preparing to write ${dataToWrite.size} bytes (data before marker) to PipedOutputStream.")
                        //Lom.n(INFO, "[getCapInputStream] Data to write (first 16 bytes hex): ${dataToWrite.take(16).toByteArray().joinToString("") { "%02x".format(it) }}")
                        pipeOut.write(dataToWrite)
                        //Lom.n(INFO, "[getCapInputStream] Successfully wrote ${dataToWrite.size} bytes to PipedOutputStream.")
                        markerFound = true
                        threadSuccess = true
                        break
                    } else {
                        // Marker not yet found, check if searchBuffer is too large (to prevent OOM)
                        if (searchBuffer.size() > END_MARKER_BYTES.size + 16384) { // Increased threshold slightly: marker + 2 * buffer_size
                            val dataToFlush = currentData // Use currentData which is searchBuffer.toByteArray()
                            // Retain a tail part that is larger than the marker itself plus some buffer
                            val tailSizeToKeep = END_MARKER_BYTES.size + buffer.size
                            val flushLength = dataToFlush.size - tailSizeToKeep

                            if (flushLength > 0) {
                                //Lom.n(INFO, "[getCapInputStream] searchBuffer too large (${searchBuffer.size()}). Flushing $flushLength bytes.")
                                //Lom.n(INFO, "[getCapInputStream] Flushing data (first 16 bytes hex): ${dataToFlush.take(16).toByteArray().joinToString("") { "%02x".format(it) }}")
                                pipeOut.write(dataToFlush, 0, flushLength)
                                searchBuffer.reset()
                                searchBuffer.write(dataToFlush, flushLength, dataToFlush.size - flushLength) // Write back the tail
                                //Lom.n(INFO, "[getCapInputStream] searchBuffer size after partial flush and re-add tail: ${searchBuffer.size()}.")
                            } else {
                                //Lom.n(INFO, "[getCapInputStream] searchBuffer large (${searchBuffer.size()}) but flushLength is not positive ($flushLength), not flushing. This might indicate tailSizeToKeep is too large or an issue.")
                            }
                        }
                        // Lom.n(INFO, "[getCapInputStream] Marker not found yet, continuing read loop.")
                    }
                }
            } catch (e: Exception) {
                //Lom.n(ERROR, "[getCapInputStream] Exception in reader thread: ${e.message}")
                // Log the stack trace for better debugging
                e.printStackTrace()
                threadSuccess = false // Explicitly mark as not successful
            } finally {
                //Lom.n(INFO, "[getCapInputStream] Entering finally block. Marker found: $markerFound, Thread success: $threadSuccess, searchBuffer size: ${searchBuffer.size()}")
                try {
                    if (!markerFound && searchBuffer.size() > 0) {
                        // This case means EOF was hit OR an exception occurred, and marker was not found.
                        // Write all remaining data from searchBuffer.
                        val remainingData = searchBuffer.toByteArray()
                        //Lom.n(INFO, "[getCapInputStream] In finally: Marker NOT found. Writing ${remainingData.size} remaining bytes from searchBuffer.")
                        //Lom.n(INFO, "[getCapInputStream] Remaining data to write (first 16 bytes hex): ${remainingData.take(16).toByteArray().joinToString("") { "%02x".format(it) }}")
                        pipeOut.write(remainingData)
                        //Lom.n(INFO, "[getCapInputStream] Successfully wrote ${remainingData.size} bytes from finally block.")
                    }
                    //Lom.n(INFO, "[getCapInputStream] Reader thread finishing. Closing PipedOutputStream (pipeOut).")
                    pipeOut.close()
                } catch (ioe: IOException) {
                    //Lom.n(ERROR, "[getCapInputStream] IOException closing PipedOutputStream in finally: ${ioe.message}")
                    ioe.printStackTrace()
                }
            }
        }.start()
        return pipedInputStream
    }

    fun indexOfByteArray(data: ByteArray, pattern: ByteArray, startIndex: Int = 0): Int {
        if (pattern.isEmpty()) return startIndex
        if (data.isEmpty()) return -1
        for (i in startIndex..(data.size - pattern.size)) {
            var match = true
            for (j in pattern.indices) {
                if (data[i + j] != pattern[j]) {
                    match = false
                    break
                }
            }
            if (match) return i
        }
        return -1
    }
}