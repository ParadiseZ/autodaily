package com.smart.autodaily.command

object ShellConstants {
    const val TAP = "input tap "
    const val START = "am start -n "
    const val CAPTURE = "screencap -p"
    const val BACK = "input keyevent BACK"
    const val STOP = "am force-stop "
    const val SWIPE = "input swipe "
}

// 2️⃣ 配置类：是否启用 root 权限
object ShellConfig {
    var useRoot: Boolean = false // 默认不启用 root
}

object ShellCommandBuilder {
    fun build(command: String): String {
        return if (ShellConfig.useRoot) {
            "su -c \"$command\""
        } else {
            command
        }
    }
    // 可选：提供更语义化的构建方法
    fun tap(x: Int, y: Int): String {
        return build("${ShellConstants.TAP}$x $y")
    }

    fun start(packageName: String): String {
        return build("${ShellConstants.START}$packageName")
    }

    fun capture(): String {
        return build(ShellConstants.CAPTURE)
    }

    fun back(): String {
        return build(ShellConstants.BACK)
    }

    fun stop(packageName: String): String {
        return build("${ShellConstants.STOP}$packageName")
    }

    fun swipe(x1: Int, y1: Int, x2: Int, y2: Int, time : Int): String {
        return build("${ShellConstants.SWIPE}$x1 $y1 $x2 $y2 $time")
    }
}