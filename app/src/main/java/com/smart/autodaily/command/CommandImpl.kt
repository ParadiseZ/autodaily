package com.smart.autodaily.command

import android.annotation.SuppressLint
import androidx.collection.IntSet
import com.smart.autodaily.data.entity.Point
import com.smart.autodaily.data.entity.ScriptActionInfo
import com.smart.autodaily.handler.INFO
import com.smart.autodaily.handler.conf
import com.smart.autodaily.handler.skipAcIds
import com.smart.autodaily.handler.skipFlowIds
import com.smart.autodaily.utils.Lom
import com.smart.autodaily.utils.partScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

suspend fun adbRebootApp(packName : String){
    conf.executor?.execVoidCommand(ShellCommandBuilder.stop(packName.substring(0, packName.indexOf("/"))))
    delay(2000)
    adbStartApp(packName)
}

fun adbStartApp(packName : String){
    conf.executor?.execVoidCommand(ShellCommandBuilder.start(packName))
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

open class AdbClick(var point: Point? = null) : Command{
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
        conf.executor?.execVoidCommand(ShellCommandBuilder.back())
        return true
    }
}

private fun exeClick(p: Point) {
    conf.executor?.execVoidCommand(ShellCommandBuilder.tap(p.x,p.y))
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
            conf.executor?.execVoidCommand(ShellCommandBuilder.swipe(it.x.toInt(),it.y.toInt(),it.width.toInt(),it.height.toInt(),1000))
        } ?: {
            res = false
        }
        return res
    }
}

class Skip : Command{
    override fun exec(sai: ScriptActionInfo): Boolean {
        Lom.d(INFO , "跳过${sai.flowId} ${sai.pageDesc}")
        skipAcIds.add(sai.id)
        //sai.skipFlag = true
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
        Thread.sleep(time)
        return true
    }
}
class Return(val type : String) : Command{
    override fun exec(sai: ScriptActionInfo): Boolean {
        return true
    }
}
class FinishFlowId(val flowId : Int = 0) : Command{
    override fun exec(sai: ScriptActionInfo): Boolean {
        return true
    }
}
/*class Check(private val setId : Int) : Command{
    override fun exec(sai: ScriptActionInfo): Boolean {
        return appDb.scriptSetInfoDao.getResultFlag(setId)
    }
}*/

class AddPosById(internal val saiId :Int): Command{
    override fun exec(sai: ScriptActionInfo): Boolean {
        return true
    }
}
class DropdownMenuNext(internal val setId :Int): Command{
    override fun exec(sai: ScriptActionInfo): Boolean {
        return true
    }
}
class MinusPosById(internal val saiId :Int): Command{
    override fun exec(sai: ScriptActionInfo): Boolean {
        return true
    }
}

class Reboot(internal val pkgName : String): Command{
    override fun exec(sai: ScriptActionInfo): Boolean {
        partScope.launch {
            adbRebootApp(pkgName)
        }
        return true
    }
}

class RmSkipAcIdList(private val acids : IntSet): Command{
    override fun exec(sai: ScriptActionInfo): Boolean {
        skipAcIds.removeAll(acids)
        return true
    }
}