package com.smart.autodaily.utils

import android.graphics.Bitmap
import com.smart.autodaily.handler.conf
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Bitmap对象池，用于复用Bitmap对象，减少内存分配和GC压力
 */
object BitmapPool {
    private const val TAG = "BitmapPool"
    private const val MAX_POOL_SIZE_PER_KEY = 2 // 每个尺寸配置组合的最大缓存数量
    private const val MAX_TOTAL_POOL_SIZE = 6 // 总缓存数量上限

    // 使用ConcurrentHashMap按键存储不同规格的Bitmap队列
    private val bitmapPools = ConcurrentHashMap<String, ConcurrentLinkedQueue<Bitmap>>()

    private fun generateKey(width: Int, height: Int): String {
        // 横竖屏统一，存储时始终让width <= height，或者根据实际情况决定是否需要区分横竖屏
        // val w = if (width <= height) width else height
        // val h = if (width <= height) height else width
        // return "${w}_${h}_${config?.name ?: "UNKNOWN"}"
        return "${width}_${height}"
    }

    /**
     * 获取一个指定规格的Bitmap对象，优先从对象池中获取
     * @param width 目标Bitmap宽度
     * @param height 目标Bitmap高度
     * @return Bitmap对象，如果对应规格的对象池为空则返回null
     */
    fun acquire(width: Int, height: Int): Bitmap? {
        val key = generateKey(width, height)
        val pool = bitmapPools[key]
        return pool?.poll()
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
        var key : String
        bitmap.let {
            key = generateKey(it.width, it.height)
        }
        val pool = bitmapPools.computeIfAbsent(key) { ConcurrentLinkedQueue<Bitmap>() }

        // 检查总池大小和单个池大小
        var currentTotalSize = 0
        bitmapPools.values.forEach { currentTotalSize += it.size }

        if (pool.size >= MAX_POOL_SIZE_PER_KEY || currentTotalSize >= MAX_TOTAL_POOL_SIZE) {
            // Lom.n(TAG, "对象池已满 (key: $key, size: ${pool.size}, total: $currentTotalSize)，直接回收Bitmap")
            bitmap.recycle()
            return false
        }

        try {
            return pool.offer(bitmap)
        } catch (e: Exception) {
            Lom.n(TAG, "回收图像到池中失败 (key: $key): ${e.message}")
            bitmap.recycle() // 确保回收
            return false
        }
    }

    /**
     * 清空对象池，回收所有Bitmap对象
     */
    fun clear() {
        bitmapPools.values.forEach { pool ->
            var bitmap: Bitmap? = pool.poll()
            while (bitmap != null) {
                if (!bitmap.isRecycled) {
                    bitmap.recycle()
                }
                bitmap = pool.poll()
            }
        }
        bitmapPools.clear()
    }

    /**
     * 获取对象池中的Bitmap总数量
     * @return 对象池中的Bitmap总数量
     */
    fun size(): Int {
        var totalSize = 0
        bitmapPools.values.forEach { pool ->
            totalSize += pool.size
        }
        return totalSize
    }
}

/**
 * 获取屏幕截图，使用Bitmap对象池优化内存使用
 * @return 屏幕截图Bitmap对象，如果截图失败则返回null
 */
fun getPicture(scale : Int) : Bitmap? {
    return conf.executor?.execCap(scale)
}