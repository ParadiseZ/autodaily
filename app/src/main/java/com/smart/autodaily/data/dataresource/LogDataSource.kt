package com.smart.autodaily.data.dataresource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.smart.autodaily.utils.logFile
import java.io.RandomAccessFile
import java.util.LinkedList
import kotlin.math.min

class LogDataSource(private var oldFileLen :Long): PagingSource<Long, String>() {

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, String> {
        return try {
            // 初始位置：文件末尾
            // 确保文件存在且可读
            if (!logFile.exists() || !logFile.canRead()) {
                return LoadResult.Page(
                    data = emptyList(),
                    prevKey = null,
                    nextKey = null
                )
            }
            var pos = params.key ?: logFile.length()
            if (oldFileLen > logFile.length()){
                pos = logFile.length()
                oldFileLen = logFile.length()
            }
            val (prePos,nexPos, lines) = getLogs(pos, params.loadSize)
            LoadResult.Page(
                data = lines,
                prevKey = prePos,
                nextKey = nexPos,
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    private fun getLogs(startPos: Long, pageSize: Int): Triple<Long?,Long?, List<String>> {
        var currentPos = startPos
        val buffer = ByteArray(4096)
        val lines = LinkedList<String>() // 使用链表提高头部插入效率
        var remainingBytes = ByteArray(0)
        RandomAccessFile(logFile, "r").use { raf ->
            while (currentPos > 0 && lines.size < pageSize) {
                val readSize = min(buffer.size.toLong(), currentPos).toInt()
                currentPos -= readSize
                raf.seek(currentPos)
                raf.readFully(buffer, 0, readSize)

                // 合并剩余字节与新读取的内容
                val chunk = remainingBytes + buffer.sliceArray(0 until readSize)
                var lineEnd = chunk.size

                // 反向扫描换行符
                for (i in chunk.size - 1 downTo 0) {
                    if (chunk[i].toInt().toChar() == '\n' ) {
                        val lineStart = if (i > 0 && chunk[i-1] == '\r'.code.toByte()) i-1 else i
                        val line = String(chunk, lineStart + 1, lineEnd - lineStart - 1)
                        lines.addFirst(line) // 保证新的行添加到链表头部
                        lineEnd = lineStart
                    }
                }
                remainingBytes = if (lineEnd > 0) chunk.copyOfRange(0, lineEnd) else ByteArray(0)
            }
            // 处理最后剩余字节（文件第一行）
            if (lines.size < pageSize && remainingBytes.isNotEmpty()) {
                lines.addFirst(String(remainingBytes))
            }
        }

        //上一页起始点
        val prevPos = if (currentPos > 0) currentPos else null
        val nextPos = if (lines.isNotEmpty()) {
            // 上一页的起始位置是当前读取的最后一行的结束位置
            startPos + lines.sumOf { it.toByteArray().size.toLong() }
        } else {
            null
        }
        return Triple(prevPos,nextPos, lines.take(pageSize))
    }

    override fun getRefreshKey(state: PagingState<Long, String>): Long {
        return logFile.length()
        /*return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey
        }*/
    }
}