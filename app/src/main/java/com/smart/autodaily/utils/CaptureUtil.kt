package com.smart.autodaily.utils

import android.graphics.Bitmap
import com.smart.autodaily.command.CAPTURE
import com.smart.autodaily.handler.ERROR
import com.smart.autodaily.handler.isRunning
import kotlinx.coroutines.cancelChildren
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Bitmap对象池，用于复用Bitmap对象，减少内存分配和GC压力
 */
object BitmapPool {
    private const val TAG = "BitmapPool"
    private const val MAX_POOL_SIZE = 4
    
    // 使用线程安全的队列存储可复用的Bitmap对象
    private val bitmapPool = ConcurrentLinkedQueue<Bitmap>()
    
    /**
     * 获取一个Bitmap对象，优先从对象池中获取
     * @return Bitmap对象，如果对象池为空则返回null
     */
    fun acquire(): Bitmap? {
        return bitmapPool.poll()
    }
    
    /**
     * 回收Bitmap对象到对象池中
     * @param bitmap 要回收的Bitmap对象
     * @return 是否成功回收
     */
    fun recycle(bitmap: Bitmap?): Boolean {
        if (bitmap == null || bitmap.isRecycled) {
            return false
        }
        
        // 如果对象池已满，则直接回收Bitmap
        if (bitmapPool.size >= MAX_POOL_SIZE) {
            bitmap.recycle()
            return false
        }
        
        try {
            // 将Bitmap添加到对象池中
            return bitmapPool.offer(bitmap)
        } catch (e: Exception) {
            Lom.n(TAG, "回收图像失败: ${e.message}")
            bitmap.recycle()
            return false
        }
    }
    
    /**
     * 清空对象池，回收所有Bitmap对象
     */
    fun clear() {
        var bitmap: Bitmap? = bitmapPool.poll()
        while (bitmap != null) {
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
            bitmap = bitmapPool.poll()
        }
    }
    
    /**
     * 获取对象池中的Bitmap数量
     * @return 对象池中的Bitmap数量
     */
    fun size(): Int {
        return bitmapPool.size
    }
}

/**
 * 获取屏幕截图，使用Bitmap对象池优化内存使用
 * @return 屏幕截图Bitmap对象，如果截图失败则返回null
 */
fun getPicture(scale : Int) : Bitmap? {
    try {
        ShizukuUtil.iUserService?.let { service ->
            // 尝试从对象池获取Bitmap
            val bitmap = service.execCap(CAPTURE,scale)
            
            // 将使用完的Bitmap放回对象池
            val oldBitmap = bitmap
            // 这里我们直接返回新的Bitmap，因为execCap每次都会创建新的Bitmap
            // 将旧的Bitmap添加到对象池中以便下次复用
            BitmapPool.recycle(oldBitmap)
            
            return bitmap
        }
        /*appCtx.assets.open("0184.jpg").use { inputStream ->
            return BitmapFactory.decodeStream(inputStream)
        }*/
        return null
    } catch (_: Exception) {
        runScope.coroutineContext.cancelChildren()
        isRunning.intValue = 0
        Lom.n(ERROR, "截图失败，停止运行")
        return null
    }
}