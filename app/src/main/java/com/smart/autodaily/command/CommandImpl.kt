package com.smart.autodaily.command

import com.smart.autodaily.data.appDb
import com.smart.autodaily.data.entity.Point
import com.smart.autodaily.data.entity.ScriptActionInfo
import com.smart.autodaily.handler.ActionHandler
import com.smart.autodaily.utils.ShizukuUtil

const val TAP = "input tap "
const val START = "am start -n "
const val CAPTURE = "screencap -p"

class AdbClick(private var point: Point? = null) : Command{
    override fun exec(sai: ScriptActionInfo): Boolean {
        var res = true
        this.point?.let {
            exeClick(it)
        } ?: {
            sai.point?.let {
                exeClick(it)
            } ?: {
                res = false
            }
        }
        sai.point = null
        return res
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
        return appDb!!.scriptSetInfoDao.getResultFlag(setId)
    }
}

