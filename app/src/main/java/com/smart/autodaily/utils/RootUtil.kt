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

    val END_MARKER_BYTES = "${ShellConstants.END_COMMAND}\n".toByteArray(Charsets.ISO_8859_1)

    //private const val STALE_DATA_CLEAR_POLL_DELAY_MS = 20L
    private const val STALE_DATA_CLEAR_MAX_ATTEMPTS = 5
    //private const val INITIAL_STALE_DATA_CLEAR_DELAY_MS = 50L

    private const val MAX_TRY_NUM = 20

    val pngHead =  byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)


    //private fun tryClearStaleDataFrom(stream: InputStream?, caller: String) {
    private fun tryClearStaleDataFrom(stream: InputStream?) {
        if (stream == null) return
        // Lom.d(TAG, "[$caller] Attempting to clear stale data...") // Example of a debug log
        val tempBuffer = ByteArray(128)
        var readAttempt = 0
        //var totalBytesCleared = 0
        try {
            // Give a brief moment for any async output to arrive
            //Thread.sleep(INITIAL_STALE_DATA_CLEAR_DELAY_MS)
            while (stream.available() > 0) {
                readAttempt++
                //val numRead = stream.read(tempBuffer)
                if (stream.read(tempBuffer) > 0) {
                    continue
                    //totalBytesCleared += numRead
                } else { // numRead == 0 (should not happen if available > 0) or numRead == -1 (EOF)
                    break
                }
                if(readAttempt > STALE_DATA_CLEAR_MAX_ATTEMPTS){
                    break
                }
                /*if (readAttempt < STALE_DATA_CLEAR_MAX_ATTEMPTS && stream.available() > 0) { // Check available again before sleep
                    //Thread.sleep(STALE_DATA_CLEAR_POLL_DELAY_MS)
                } else {
                    break
                }*/
            }
            // if (totalBytesCleared > 0) {
            //     Lom.n(INFO, "[$caller] Cleared $totalBytesCleared bytes of stale data.")
            // }
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
            //Lom.n(ERROR, "[$caller] Interrupted while clearing stale data: ${e.message}")
        } catch (e: Exception) {
            //Lom.n(ERROR, "[$caller] Exception while clearing stale data: ${e.message}")
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
                //Lom.n(INFO, "[RootUtil] Starting su process...")
                process = Runtime.getRuntime().exec("su")
                inputStream = process?.inputStream
                outputStream = OutputStreamWriter(process?.outputStream, Charsets.UTF_8)
                //Lom.n(INFO, "[RootUtil] su process started.")
            } catch (e: Exception) {
                //Lom.n(ERROR, "[RootUtil] Failed to start su process: ${e.message}")
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

    fun close() {
        //Lom.n(INFO, "[RootUtil] Closing su process and streams.")
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
             //Lom.n(ERROR, "[execVoidCommand] outputStream is null. Cannot execute: $command")
             //return // Or throw exception
            throw Exception("outputStream is null")
        }
        try {
            //Lom.n(INFO, "[execVoidCommand] Executing: $command")
            //outputStream?.write("$command\n") // Ensure newline for command execution
            //outputStream?.write("echo ${ShellConstants.END_COMMAND}\n") // Standard end marker
            outputStream?.write(command)
            outputStream?.flush()
            
            // Basic consumption of the marker, can be improved if needed
            // This is a simplified wait, for more critical commands, a robust read-until-marker is better.
            //Thread.sleep(200) // Short fixed delay for command to complete and marker to be echoed
            //tryClearStaleDataFrom(inputStream, "execVoidCommand") // Clear餘留
            tryClearStaleDataFrom(inputStream) // Clear餘留
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
            //Lom.n(ERROR, "[execVoidCommand] Interrupted: $command. Error: ${e.message}")
        } catch (_: Exception) {
            //Lom.n(ERROR, "[execVoidCommand] Failed to execute: $command. Error: ${e.message}")
            // Consider if close() should be called here on failure
            throw Exception("exec $command failed")
        }
    }

    fun execCap(command: String, width:Int, height:Int, scale : Int): Bitmap {
        start()
        if (outputStream == null || inputStream == null) {
            //throw IllegalStateException("RootUtil su process not started or streams are null for execCap.")
            throw IllegalStateException("su process error")
        }
        // Lom.n(INFO, "[execCap] Preparing to execute: $command")

        var finalImageData: ByteArray? = null
        var attempt = 0
        //var lastKnownImageDataSize = 0
        while (attempt < MAX_TRY_NUM) {
            attempt++
            tryClearStaleDataFrom(inputStream)
            //Lom.n(INFO, "[execCap] Attempt #$attempt to capture screen.")
            /*if (attempt > 1) {
                // Lom.n(INFO, "[execCap] Retrying: Clearing stale data before attempt #$attempt")
                tryClearStaleDataFrom(inputStream)
                //Thread.sleep(150)
            } else {
                tryClearStaleDataFrom(inputStream)
            }*/

            val currentImageData = getCapInputStream(command).readBytes()
            //lastKnownImageDataSize = currentImageData.size

            // Lom.n(INFO, "[execCap] Attempt #$attempt: data size: ${currentImageData.size}")
            var isValidAttempt = false
            if (currentImageData.isNotEmpty()) {
                // Only log extensive details if it's NOT a valid attempt, to reduce noise on success
                val isValidPngHeader = currentImageData.size >= 8 && 
                                     currentImageData.take(8).toByteArray().contentEquals(
                                         pngHead
                                     )

                if (isValidPngHeader && currentImageData.size > 1000) { 
                    isValidAttempt = true
                }/*else {
                    // Log details on failure to validate
                    Lom.n(ERROR, "[execCap] Attempt #$attempt: Invalid data. Size: ${currentImageData.size}, PNG Header OK: $isValidPngHeader")
                    if (currentImageData.size >= 8 && !isValidPngHeader) {
                         Lom.n(ERROR, "[execCap] Invalid PNG header (first 8 bytes hex): ${currentImageData.take(8).toByteArray().joinToString("") { "%02x".format(it) }}")
                    }
                     // Optionally log first few bytes as string if small and not PNG
                    // if (!isValidPngHeader && currentImageData.size < 200) {
                    //     Lom.n(ERROR, "[execCap] Data (first ~100 bytes as string): ${String(currentImageData.take(100).toByteArray(), Charsets.UTF_8)}")
                    // }
                }*/
            }/* else {
                Lom.n(ERROR, "[execCap] Attempt #$attempt: Received empty data.")
            }*/

            if (isValidAttempt) {
                finalImageData = currentImageData
                //Lom.n(INFO, "[execCap] Successfully captured screen on attempt #$attempt. Size: ${finalImageData.size}")
                break 
            }/* else {
                //Lom.n(ERROR, "[execCap] Attempt #$attempt failed. Retrying if attempts left ($attempt/$maxAttempts).")
                if (attempt < maxAttempts) {
                    Thread.sleep(300) 
                }
            }*/
        }

        if (finalImageData == null) {
            /*val errorMessage = "Failed to capture screen after $maxAttempts attempts. Last data size: $lastKnownImageDataSize"
            Lom.n(ERROR, "[execCap] $errorMessage")
            throw IllegalStateException(errorMessage)*/
            throw IllegalStateException("Failed to capture screen after $MAX_TRY_NUM attempts")
        }

        // Conditional saving of image for debugging - typically commented out in production
        /*
        Lom.n(INFO, "[execCap] Saving screencap data (size: ${finalImageData.size}) for debugging.")
        try {
            val timestamp = System.currentTimeMillis()
            val cacheDir = appCtx.externalCacheDir ?: appCtx.cacheDir
            val file = File(cacheDir, "screencap_debug_${timestamp}_${finalImageData.size}bytes.png")
            java.io.FileOutputStream(file).use { fos ->
                fos.write(finalImageData)
            }
            Lom.n(INFO, "[execCap] Screencap data saved to: ${file.absolutePath}")
        } catch (e: Exception) {
            Lom.n(ERROR, "[execCap] Failed to save debug screencap data: ${e.message}")
        }
        */

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
                //Lom.n(ERROR, "[execCap] BitmapFactory.decodeByteArray returned null for ${finalImageData.size} bytes.")
                // BitmapPool handling: if acquiredBitmapFromPool was used and decoding failed, 
                // it's generally safe to recycle it. If it wasn't used (options.inBitmap was null), this does nothing.
                BitmapPool.recycle(acquiredBitmapFromPool) // Safe to call even if null
                throw IllegalStateException("BitmapFactory.decodeByteArray returned null after successful data capture.")
            }

            if (acquiredBitmapFromPool != null && decodedBitmap !== acquiredBitmapFromPool) {
                BitmapPool.recycle(acquiredBitmapFromPool) // Recycle if a new bitmap was decoded
            }
            return decodedBitmap
    
        } catch (e: Exception) {
            BitmapPool.recycle(acquiredBitmapFromPool) // Ensure recycle on any exception during decoding
            //Lom.n(ERROR, "[execCap] Exception during bitmap decoding: ${e.message}")
            throw e // Re-throw
        }
    }
}