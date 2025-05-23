package com.smart.autodaily.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader


object RootUtil {

    private const val TAG = "RootUtil"
    fun execLine(command: String): String? {
        var result: String? = null

        // 尝试 su -c 方式
        result = try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            readResult(process)
        } catch (_: Exception) {
            null
        }

        // 如果失败，尝试交互式方式
        if (result == null) {
            result = try {
                val process = Runtime.getRuntime().exec("su")
                val outputStream = process.outputStream
                outputStream.write("$command\nexit\n".toByteArray())
                outputStream.flush()
                readResult(process)
            } catch (e: Exception) {
                null
            }
        }

        return result
    }

    fun execVoidCommand(command: String) {
        try {
            Runtime.getRuntime().exec(arrayOf("su", "-c", command))
        } catch (e: Exception) {
            println("execVoidCommand error: ${e.message}")
        }
    }

    fun execArr(command: Array<out String>?): String? {
        if (command.isNullOrEmpty()) return null
        val fullCommand = command.joinToString(" ") { escapeArg(it) }
        return execLine(fullCommand)
    }

    fun execCap(command: String?,scale : Int): Bitmap {
        return readBitmap(Runtime.getRuntime().exec(command),scale)
    }

    private fun escapeArg(arg: String): String {
        if (arg.contains(' ')) {
            return "\"$arg\""
        }
        return arg
    }
    fun readBitmap(process: Process,scale : Int): Bitmap {
        val inputStream = process.inputStream
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()
        return bitmap
    }


    private fun readResult(process: Process): String {
        val stringBuilder = StringBuilder()
        try {
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            while ((reader.readLine().also { line = it }) != null) {
                stringBuilder.append(line).append("\n")
            }
            reader.close()
            process.waitFor()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return stringBuilder.toString()
    }


    /**
     * 执行需要root权限的命令.
     *
     * @param command 要执行的命令字符串.
     * @return 命令执行的结果 (true 表示成功, false 表示失败).
     */
    fun executeCommand(command: String): Boolean {
        var process: Process? = null
        var os: DataOutputStream? = null
        return try {
            // 启动一个带有 'su' 的新进程
            process = Runtime.getRuntime().exec("su")
            os = DataOutputStream(process.outputStream)

            // 发送要执行的命令到 shell
            os.writeBytes(command + "\n")
            os.flush()

            // 发送 'exit' 以结束 shell 会话
            os.writeBytes("exit\n")
            os.flush()

            // 等待命令执行完成
            val exitCode = process.waitFor()
            exitCode == 0
        } catch (e: IOException) {
            Log.e(TAG, "Failed to execute command", e)
            false
        } catch (e: InterruptedException) {
            Log.e(TAG, "Process interrupted", e)
            false
        } finally {
            // 关闭流和进程
            os?.close()
            process?.destroy()
        }
    }
}