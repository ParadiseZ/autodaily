package com.smart.autodaily.service

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.smart.autodaily.IUserService
import com.smart.autodaily.utils.BitmapPool.acquire
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

    fun readBitmap(process: Process,scale: Int): Bitmap {
        //Log.d("UserService", "Reading bitmap from process")
        val inputStream = process.inputStream
        
        // 尝试从BitmapPool获取可复用的Bitmap
        val pooledBitmap = acquire()
        
        // 如果有可复用的Bitmap，尝试复用它
        val bitmap = if (pooledBitmap != null && !pooledBitmap.isRecycled) {
            try {
                // 使用BitmapFactory.Options的inBitmap选项复用Bitmap
                val options = BitmapFactory.Options().apply {
                    inBitmap = pooledBitmap
                    inMutable = true
                    inSampleSize = scale
                }
                BitmapFactory.decodeStream(inputStream, null, options)
            } catch (_: Exception) {
                // 如果复用失败，则创建新的Bitmap
                BitmapFactory.decodeStream(inputStream)
            }
        } else {
            // 如果没有可复用的Bitmap，则创建新的Bitmap
            BitmapFactory.decodeStream(inputStream)
        }
        
        inputStream.close()
        return bitmap!!
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