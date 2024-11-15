package com.smart.autodaily.command

import com.smart.autodaily.data.appDb
import com.smart.autodaily.data.entity.Point
import com.smart.autodaily.data.entity.ScriptActionInfo
import com.smart.autodaily.handler.ActionHandler
import com.smart.autodaily.utils.ShizukuUtil
import kotlinx.coroutines.delay

const val TAP = "input tap "
const val START = "am start -n "
const val CAPTURE = "screencap -p"
const val BACK = "input keyevent BACK "
const val STOP = "am force-stop "
const val SWIPE = "input swipe  "

suspend fun adbRebootApp(packName : String){
    println("restart app")
    ShizukuUtil.iUserService?.execVoidComand(STOP +packName.substring(0, packName.indexOf("/")))
    delay(2000)
    adbStartApp(packName)
}

fun adbStartApp(packName : String){
    ShizukuUtil.iUserService?.execLine(START + packName)
}

class Operation(val type: Int, val operation : Command) : Command{
    override fun exec(sai: ScriptActionInfo): Boolean {
        return operation.exec(sai)
    }
}

open class AdbClick(private var point: Point? = null) : Command{
    override fun exec(sai: ScriptActionInfo): Boolean {
        var res = false
        when{
            this.point !=null ->{
                exeClick(this.point!!)
                res = true
            }
            sai.point !=null ->{
                exeClick(sai.point!!)
                sai.point = null
                res = true
            }
        }
        return res
    }
}
class AdbPartClick(var type :String? = null,var part : Int = 0,var idx : Int = 0): AdbClick()

class AdbBack : Command{
    override fun exec(sai: ScriptActionInfo): Boolean {
        ShizukuUtil.iUserService?.execVoidComand(BACK)
        return true
    }
}

private fun exeClick(p: Point) {
    val command = TAP + "${p.x + ActionHandler.randomXRange} ${p.y + ActionHandler.randomYRange}"
    //println(command)
    ShizukuUtil.iUserService?.execVoidComand(command)
}
class AdbSumClick(private val point: Point) : Command{
    override fun exec(sai: ScriptActionInfo): Boolean {
        var res = true
        sai.point?.let {
            it.x += point.x
            it.y += point.y
            exeClick(it)
        } ?: {
            res =  false
        }
        return res
    }
}

class AdbSwipe : Command{
    override fun exec(sai: ScriptActionInfo): Boolean {
        var res = true
        sai.swipePoint?.let {
            val command = "$SWIPE ${it.x} ${it.y} ${it.width} ${it.height} 1000"
            ShizukuUtil.iUserService?.execVoidComand(command)
        } ?: {
            res = false
        }
        return res
    }
}

class Skip : Command{
    override fun exec(sai: ScriptActionInfo): Boolean {
        sai.skipFlag = true
        return false
    }
}

class Sleep(private var time : Long = 1000) : Command{
    override fun exec(sai: ScriptActionInfo): Boolean {
        Thread.sleep(time)
        return true
    }
}
class Return(val type : String) : Command{
    override fun exec(sai: ScriptActionInfo): Boolean {
        return true
    }
}
class Check(private val setId : Int) : Command{
    override fun exec(sai: ScriptActionInfo): Boolean {
        return appDb.scriptSetInfoDao.getResultFlag(setId)
    }
}

