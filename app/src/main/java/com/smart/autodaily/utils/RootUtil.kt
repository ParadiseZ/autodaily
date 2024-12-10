package com.smart.autodaily.utils

import android.util.Log
import java.io.DataOutputStream
import java.io.IOException


object RootUtil {

    private const val TAG = "RootUtil"

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