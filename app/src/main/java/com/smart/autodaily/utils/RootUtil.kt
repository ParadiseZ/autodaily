package com.smart.autodaily.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.smart.autodaily.command.ShellConstants
import com.smart.autodaily.handler.INFO
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStreamWriter
import java.io.PipedInputStream
import java.io.PipedOutputStream


object RootUtil {
    private var process: Process? = null
    private var outputStream: OutputStreamWriter? = null
    private var inputStream: InputStream? = null

    private val END_MARKER_BYTES = "${ShellConstants.END_COMMAND}\n".toByteArray(Charsets.ISO_8859_1)
    private const val STALE_DATA_CLEAR_MAX_ATTEMPTS = 5
    private const val MAX_TRY_NUM = 20
    private val pngHead =  byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)


    private fun tryClearStaleDataFrom(stream: InputStream?) {
        if (stream == null) return
        val tempBuffer = ByteArray(128)
        var readAttempt = 0
        try {
            while (stream.available() > 0) {
                readAttempt++
                if (stream.read(tempBuffer) > 0) {
                    continue
                } else { // numRead == 0 (should not happen if available > 0) or numRead == -1 (EOF)
                    break
                }
                if(readAttempt > STALE_DATA_CLEAR_MAX_ATTEMPTS){
                    break
                }
            }
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        } catch (e: Exception) {
            throw e
        }
    }

    fun rootValid() : Boolean{
        if(process == null){
            try {
                start()
            }catch (_ : Exception){
                return false
            }
        }
        return true
    }

    fun start() {
        if (process == null) {
            try {
                process = Runtime.getRuntime().exec("su")
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

    // Helper function to find a byte array within another byte array
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

    private fun getCapInputStream(command: String): InputStream{
        val pipedInputStream = PipedInputStream()
        val pipeOut = PipedOutputStream(pipedInputStream)
        val searchBuffer = ByteArrayOutputStream()

        Thread {
            var threadSuccess = false
            var markerFound = false
            try {
                outputStream?.write(command)
                outputStream?.flush()
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (true) {
                    bytesRead = inputStream?.read(buffer) ?: -1

                    if (bytesRead == -1) {
                        break // Exit loop, finally block will handle writing remaining buffer if necessary
                    }

                    searchBuffer.write(buffer, 0, bytesRead)
                    val currentData = searchBuffer.toByteArray()
                    val markerPos = indexOfByteArray(currentData, END_MARKER_BYTES)
                    if (markerPos != -1) {
                        val dataToWrite = currentData.copyOfRange(0, markerPos)
                        pipeOut.write(dataToWrite)
                        markerFound = true
                        threadSuccess = true
                        break
                    } else {
                        if (searchBuffer.size() > END_MARKER_BYTES.size + 16384) { // Increased threshold slightly: marker + 2 * buffer_size
                            val dataToFlush = currentData // Use currentData which is searchBuffer.toByteArray()
                            val tailSizeToKeep = END_MARKER_BYTES.size + buffer.size
                            val flushLength = dataToFlush.size - tailSizeToKeep
                            if (flushLength > 0) {
                                pipeOut.write(dataToFlush, 0, flushLength)
                                searchBuffer.reset() 
                                searchBuffer.write(dataToFlush, flushLength, dataToFlush.size - flushLength) // Write back the tail
                            }
                        }
                    }
                }
            } catch (e: Exception){
                e.printStackTrace()
                threadSuccess = false // Explicitly mark as not successful
            } finally {
                try {
                    if (!markerFound && searchBuffer.size() > 0) {
                        val remainingData = searchBuffer.toByteArray()
                        pipeOut.write(remainingData)
                    }
                    pipeOut.close()
                } catch (ioe: IOException) {
                    ioe.printStackTrace()
                }
            }
        }.start()
        return pipedInputStream
    }

    fun close() {
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
        Lom.d(INFO, "[RootUtil] su process and streams closed.")
    }

    fun execVoidCommand(command: String) {
        start()
        if (outputStream == null) {
            throw Exception("outputStream is null")
        }
        try {
            outputStream?.write(command)
            outputStream?.flush()
            tryClearStaleDataFrom(inputStream) // Clear餘留
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        } catch (_: Exception) {
            throw Exception("exec $command failed")
        }
    }

    fun execCap(command: String, width:Int, height:Int, scale : Int): Bitmap {
        start()
        if (outputStream == null || inputStream == null) {
            throw IllegalStateException("su process error")
        }
        var finalImageData: ByteArray? = null
        var attempt = 0
        while (attempt < MAX_TRY_NUM) {
            attempt++
            tryClearStaleDataFrom(inputStream)

            val currentImageData = getCapInputStream(command).readBytes()

            var isValidAttempt = false
            if (currentImageData.isNotEmpty()) {
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
            throw IllegalStateException("Failed to capture screen after $MAX_TRY_NUM attempts")
        }

        var acquiredBitmapFromPool: Bitmap? = null
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = false
            inSampleSize = scale
            inMutable = true
        }
        try {
            acquiredBitmapFromPool = BitmapPool.acquire(width, height)
            if (acquiredBitmapFromPool != null) {
                options.inBitmap = acquiredBitmapFromPool
            }
            val decodedBitmap = BitmapFactory.decodeByteArray(finalImageData, 0, finalImageData.size, options)

            if (decodedBitmap == null) {
                BitmapPool.recycle(acquiredBitmapFromPool) // Safe to call even if null
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