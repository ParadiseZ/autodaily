package com.smart.autodaily.utils

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.smart.autodaily.handler.ERROR
import com.smart.autodaily.handler.INFO
import com.smart.autodaily.handler.RunScript
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import splitties.init.appCtx
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.RandomAccessFile

val logFile by lazy {
    File("${appCtx.getExternalFilesDir("")}/autodaily.log")
}
val logSet by lazy {
    mutableStateOf("关闭")
}
object Lom {
    private const val MAX_LOG_SIZE = 512 * 1024L// 2MB 2 * 1024 * 1024L
    private val dateFormat = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss")
    private val logChannel = Channel<String>(256)
    private var enableLog = false

    fun d(category: String, message: String) {
        logPrint(category,message)
        if (enableLog && logSet.value=="详细"){
            val msg = "${LocalDateTime.now().format(dateFormat)} [$category] $message"
            logChannel.trySend(msg)
        }
    }
    fun d(category: String, message: String, data: Set<Short>) {
        if (data.isNotEmpty()){
            val msg = StringBuilder()
            msg.append(message)
            msg.append(" ")
            data.forEach {
                msg.append("${it},")
            }
            d(category,msg.toString())
        }else{
            d(category, message)
        }
    }

    fun n(category: String, message: String) {
        logPrint(category,message)
        if (enableLog){
            val msg = "${LocalDateTime.now().format(dateFormat)} [$category] $message"
            logChannel.trySend(msg)
        }
    }

    private fun logPrint(category: String, message: String){
        if (category== ERROR){
            Log.e(category, message)
        }else{
            Log.d(category, message)
        }
    }

    fun waitWriteLog(){
        logScope.coroutineContext.cancelChildren()
        enableLog = false
        logSet.value =  RunScript.globalSetMap.value[7]?.setValue?:"关闭"
        n(INFO , "日志设置：${logSet.value}")
        if ("关闭" == logSet.value){
            enableLog = true
            writeLogToFile()
        }
    }



    private fun writeLogToFile() {
        logScope.launch {
            var fileWriter = FileWriter(logFile, true)
            try {
                while (true) {
                    val message = logChannel.receive() // 挂起点，可能被取消
                    fileWriter.write("$message\n")
                    fileWriter.flush()
                    limitFileSize(fileWriter,logFile)?.let {
                        fileWriter = it
                    }
                }
            } finally {
                fileWriter.close() // 协程取消时，finally 会执行
            }
        }
    }

    private fun limitFileSize(fileWriter : FileWriter,file: File) : FileWriter?{
        if (file.length() > MAX_LOG_SIZE) {
            fileWriter.close()
            // 创建临时文件
            val tempFile = File("${appCtx.getExternalFilesDir("")}/temp.log")
            deleteFile(tempFile)
            // 截取文件的后半部分
            RandomAccessFile(file, "r").use { raf ->
                val channel = raf.channel
                val size = channel.size()
                val start = size - (size shr 1) // 从文件的后半部分开始
                val position = if (start < 0) 0 else start
                FileOutputStream(tempFile).use { fos ->
                    val destChannel = fos.channel
                    channel.transferTo(position, MAX_LOG_SIZE, destChannel)
                }
            }
            deleteFile(file)
            // 删除旧文件并重命名临时文件
            if (!file.delete()){
                n( ERROR,"删除原日志文件失败！")
            }
            if(!tempFile.renameTo(file)){
                n( ERROR,"重命名临时日志文件失败！")
            }
            return FileWriter(file, true)
        }
        // 如果文件大小未超过限制，返回当前的 FileWriter
        return null
    }
}


