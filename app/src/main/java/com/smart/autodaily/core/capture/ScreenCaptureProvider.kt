package com.smart.autodaily.core.capture

import android.graphics.Bitmap
// com.smart.autodaily.data.entity.Rect is not used in the provided interface methods, so not imported for now.

interface ScreenCaptureProvider {
    /**
     * Captures the current screen and returns it as a Bitmap.
     * @return A Bitmap of the screen, or null if capture failed.
     */
    suspend fun captureScreenBitmap(): Bitmap?

    /**
     * Gets colors from a specified region of a provided bitmap.
     * This is useful for color checking without performing a new screen capture.
     *
     * @param sourceBitmap The bitmap to extract colors from.
     * @param centerX The center X coordinate of the target area for color check.
     * @param centerY The center Y coordinate of the target area for color check.
     * @param regionWidth The width of the region around the center point to sample colors from.
     * @param regionHeight The height of the region around the center point to sample colors from.
     * @return A list of unique colors (Int representation of Color) found in the specified region, or empty list if issues.
     */
    suspend fun getRegionColors(sourceBitmap: Bitmap, centerX: Int, centerY: Int, regionWidth: Int, regionHeight: Int): List<Int>
    
    /**
     * Gets the MD5 hash of the given bitmap.
     * @param bitmap The bitmap to hash.
     * @return MD5 hash string, or null if hashing failed.
     */
    suspend fun getBitmapHash(bitmap: Bitmap): String?
}
