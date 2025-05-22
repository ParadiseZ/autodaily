package com.smart.autodaily.core.capture

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import com.smart.autodaily.utils.ShizukuUtil // Assuming ShizukuUtil.capture() is available
import java.io.ByteArrayOutputStream
import java.security.MessageDigest

class ScreenCaptureProviderImpl(
    // Context might not be strictly needed if ShizukuUtil is static and self-contained
    // private val context: Context 
) : ScreenCaptureProvider {

    companion object {
        private const val TAG = "ScreenCaptureProvider"
    }

    override suspend fun captureScreenBitmap(): Bitmap? {
        return try {
            // Adapting from RunScript.getPicture() which used ShizukuUtil.capture()
            // Assuming ShizukuUtil.capture() directly returns a Bitmap?
            val bitmap = ShizukuUtil.capture() 
            if (bitmap == null) {
                Log.e(TAG, "ShizukuUtil.capture() returned null.")
            }
            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Error capturing screen via ShizukuUtil: ${e.message}", e)
            null
        }
    }

    override suspend fun getRegionColors(
        sourceBitmap: Bitmap,
        centerX: Int,
        centerY: Int,
        regionWidth: Int,
        regionHeight: Int
    ): List<Int> {
        if (regionWidth <= 0 || regionHeight <= 0) {
            Log.w(TAG, "getRegionColors: regionWidth and regionHeight must be positive.")
            return emptyList()
        }

        val uniqueColors = mutableSetOf<Int>()

        // Calculate the top-left corner of the region
        val startX = centerX - regionWidth / 2
        val startY = centerY - regionHeight / 2

        // Ensure the region is within the bitmap bounds
        val clampedStartX = startX.coerceIn(0, sourceBitmap.width -1)
        val clampedStartY = startY.coerceIn(0, sourceBitmap.height -1)
        
        // Calculate actual width and height to iterate, considering bitmap boundaries
        val iterWidth = (clampedStartX + regionWidth).coerceAtMost(sourceBitmap.width) - clampedStartX
        val iterHeight = (clampedStartY + regionHeight).coerceAtMost(sourceBitmap.height) - clampedStartY

        if (iterWidth <= 0 || iterHeight <= 0) {
            Log.w(TAG, "getRegionColors: Calculated iteration width/height is zero or negative. Region might be fully outside bitmap.")
            return emptyList()
        }
        
        try {
            // Pre-allocate buffer for getPixels if performance is critical for larger regions
            // For now, getPixel one by one for simplicity.
            for (yOffset in 0 until iterHeight) {
                for (xOffset in 0 until iterWidth) {
                    val currentX = clampedStartX + xOffset
                    val currentY = clampedStartY + yOffset
                    // Double check bounds, though coerceIn and iterWidth/Height should handle it.
                    if (currentX < sourceBitmap.width && currentY < sourceBitmap.height) {
                         uniqueColors.add(sourceBitmap.getPixel(currentX, currentY))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in getRegionColors while accessing pixels: ${e.message}", e)
            // Return whatever unique colors were found so far, or empty list on critical error
            return uniqueColors.toList() 
        }
        
        return uniqueColors.toList()
    }

    override suspend fun getBitmapHash(bitmap: Bitmap): String? {
        return try {
            val baos = ByteArrayOutputStream()
            // Use PNG compression; it's lossless. Quality is ignored for PNG.
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
            val bytes = baos.toByteArray()
            
            val md = MessageDigest.getInstance("MD5")
            val digest = md.digest(bytes)
            digest.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating MD5 hash for bitmap: ${e.message}", e)
            null
        }
    }
}
