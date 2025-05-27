package com.smart.autodaily.command

object ShellConstants {
    const val END_COMMAND = "_MY_YES_" // More unique EOF marker
    const val CAPTURE = "screencap -p" // Redirect stderr
    const val CAP_ROOT = "$CAPTURE 2>/dev/null; echo $END_COMMAND\n" // Redirect stderr
    const val TAP = "input tap "
    const val START = "am start -n "
    const val BACK = "input keyevent BACK"
    const val STOP = "am force-stop "
    const val SWIPE = "input swipe "
}

// 2️⃣ 配置类：是否启用 root 权限
object ShellConfig {
    var useRoot: Boolean = false // 默认不启用 root
}

object ShellCommandBuilder {
    // 可选：提供更语义化的构建方法
    fun tap(x: Int, y: Int): String {
        return "${ShellConstants.TAP}$x $y\n"
    }

    fun start(packageName: String): String {
        return "${ShellConstants.START}$packageName\n"
    }

    fun capture(): String {
        return if(ShellConfig.useRoot) ShellConstants.CAP_ROOT else ShellConstants.CAPTURE
    }

    fun back(): String {
        return ShellConstants.BACK+"\n"
    }

    fun stop(packageName: String): String {
        return "${ShellConstants.STOP}$packageName\n"
    }

    fun swipe(x1: Int, y1: Int, x2: Int, y2: Int, time : Int): String {
        return "${ShellConstants.SWIPE}$x1 $y1 $x2 $y2 $time\n"
    }
}