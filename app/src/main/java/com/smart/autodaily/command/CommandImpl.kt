package com.smart.autodaily.command

import android.annotation.SuppressLint
import com.smart.autodaily.data.entity.Point
import com.smart.autodaily.data.entity.ScriptActionInfo
import com.smart.autodaily.handler.ActionHandler
import com.smart.autodaily.handler.INFO
import com.smart.autodaily.handler.allActionMap
import com.smart.autodaily.handler.conf
import com.smart.autodaily.handler.skipAcIds
import com.smart.autodaily.handler.skipFlowIds
import com.smart.autodaily.utils.Lom
import com.smart.autodaily.utils.ShizukuUtil
import kotlinx.coroutines.delay

const val TAP = "input tap "
const val START = "am start -n "
const val CAPTURE = "screencap -p"
const val BACK = "input keyevent BACK "
const val STOP = "am force-stop "
const val SWIPE = "input swipe  "

suspend fun adbRebootApp(packName : String){
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

class SkipFlowId(private val skipFlowId :Int): Command{
    override fun exec(sai: ScriptActionInfo): Boolean {
        skipFlowIds.add(skipFlowId)
        return true
    }
}
class RmSkipFlowId(private val skipFlowId :Int): Command{
    override fun exec(sai: ScriptActionInfo): Boolean {
        skipFlowIds.remove(skipFlowId)
        return true
    }
}

class SkipAcId(private val skipAcId :Int): Command{
    override fun exec(sai: ScriptActionInfo): Boolean {
        skipAcIds.add(skipAcId)
        return true
    }
}
class RmSkipAcId(private val skipAcId :Int): Command{
    override fun exec(sai: ScriptActionInfo): Boolean {
        skipAcIds.remove(skipAcId)
        return true
    }
}

class NotFlowId(val notFlowId: Int) : Command{
    override fun exec(sai: ScriptActionInfo): Boolean {
        return true
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
        Lom.d(INFO , "跳过${sai.flowId} ${sai.pageDesc}")
        sai.skipFlag = true
        return false
    }
}

class Sleep(private var time : Long = 1000L) : Command{
    @SuppressLint("DefaultLocale")
    override fun exec(sai: ScriptActionInfo): Boolean {
        Lom.d(
            INFO,
            "⏳${if (time > 60000) {
                String.format("%.1f分钟", time.toDouble() / 60000.0)
            } else {
                String.format("%.1f秒", time.toDouble() / 1000.0)
            }}"
        )
        if (conf.capture?.isRecycled== false){
            conf.capture?.recycle()
        }
        Thread.sleep(time)
        return true
    }
}
class Return(val type : String,val flowId : Int = 0) : Command{
    override fun exec(sai: ScriptActionInfo): Boolean {
        return true
    }
}
/*class Check(private val setId : Int) : Command{
    override fun exec(sai: ScriptActionInfo): Boolean {
        return appDb.scriptSetInfoDao.getResultFlag(setId)
    }
}*/

class AddPosById(private val saiId :Int): Command{
    override fun exec(sai: ScriptActionInfo): Boolean {
        allActionMap.get(saiId)?.let {
            it.labelPos += 1
        }
        return true
    }
}