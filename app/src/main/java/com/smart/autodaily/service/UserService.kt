package com.smart.autodaily.service

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.smart.autodaily.IUserService
import java.io.BufferedReader
import java.io.IOException
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

    override fun execCap(command:String?): Bitmap {
        return  readBitmap(Runtime.getRuntime().exec(command))
    }

    @Throws(IOException::class, InterruptedException::class)
    fun readBitmap(process: Process): Bitmap {
        val inputStream = process.inputStream
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        return bitmap
    }

    @Throws(IOException::class, InterruptedException::class)
    fun readResult(process: Process): String {
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
    }
}