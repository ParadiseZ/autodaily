package com.smart.autodaily.command

import android.annotation.SuppressLint
import androidx.collection.IntSet
import com.smart.autodaily.data.entity.DetectResult
import com.smart.autodaily.data.entity.Point
import com.smart.autodaily.data.entity.ScriptActionInfo
import com.smart.autodaily.handler.ERROR
import com.smart.autodaily.handler.INFO
import com.smart.autodaily.handler.conf
import com.smart.autodaily.handler.getPoint
import com.smart.autodaily.handler.skipAcIds
import com.smart.autodaily.handler.skipFlowIds
import com.smart.autodaily.utils.Lom
import com.smart.autodaily.utils.partScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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
//relation find and click
class RelFAC(val lossX : Int,val lossY : Int, val direct : Int, val idx :Int): Command{
    val errorValue = if(conf.capScale == 1){20} else 10
    lateinit var ocrRes: Array<DetectResult>
    fun setDetectRes( dRes: Array<DetectResult>){
        ocrRes = dRes
    }
    override fun exec(sai: ScriptActionInfo): Boolean {
        var firFilter = ocrRes.filter { item ->
            if (item.ocr == null) {
                false
            } else {
                val labelMatch = item.ocr.label.containsAll(sai.txtFirstLab)
                val colorMatch = sai.hsv.isEmpty() || item.ocr.colorSet.containsAll(sai.hsv)
                labelMatch && colorMatch
            }
        }
        if (firFilter.isEmpty()) {
            Lom.d(ERROR, "RelFAC_saiId:${sai.id},颜色匹配,目标${sai.hsv}")
            return false
        }
        //val secFilter: List<DetectResult>
        if(firFilter.size > 1) {
            val minSize = firFilter.minOf { it.ocr!!.label.size }
            firFilter = firFilter.filter {
                it.ocr!!.label.size == minSize
            }
        }
        //非第一个，排序
        var target : DetectResult
        if (sai.labelPos>0 && firFilter.size > 1){
            firFilter = firFilter.sortedWith(
                compareBy(
                    { (it.yCenter / errorValue).roundToInt()} ,{ (it.xCenter / errorValue).roundToInt() }
                )
            )
            val idx = (sai.labelPos-1).coerceAtMost(firFilter.size-1)
            target = firFilter[idx]
        }else{
            target = firFilter[0]
        }
        val rel = ocrRes.filter { item ->
            when (direct){
                2 ->  (item.rect.x > target.rect.x - lossX && item.rect.x < target.rect.x ) &&
                        (item.rect.y > target.rect.y - lossY && item.rect.y < target.rect.y)
                else -> false
            }
        }.sortedWith (
            compareBy(
                { (it.yCenter / errorValue).roundToInt()} ,{ (it.xCenter / errorValue).roundToInt() }
            )
        )
        if (rel.isEmpty()){
            return false
        }
        val idxT =  idx.coerceAtMost(firFilter.size-1)
        val secTxt = sai.txtLabel!!.split("|")[1].split(",").map { labelStr -> labelStr.toShort() }.toSet()
        rel[idxT].ocr?.label?.let {
            if(rel.size > idx && it.containsAll(secTxt)){
                sai.point = getPoint(rel[idxT])
            }else{
                return false
            }
        }
        return true
    }
}

class RelLabFAC(val lossX : Int,val lossY : Int, val direct : Int, val idx :Int): Command{
    val errorValue = if(conf.capScale == 1){20} else 10
    lateinit var ocrRes: Array<DetectResult>
    fun setDetectRes( dRes: Array<DetectResult>){
        ocrRes = dRes
    }
    override fun exec(sai: ScriptActionInfo): Boolean {
        var firFilter = ocrRes.filter {
            it.label == sai.intFirstLab
        }
        if (firFilter.isEmpty()) {
            Lom.d(ERROR, "RelLabFAC_saiId:${sai.id},颜色匹配,目标${sai.hsv}")
            return false
        }
        //非第一个，排序
        var target : DetectResult
        if (sai.labelPos>0 && firFilter.size>1){
            firFilter = firFilter.sortedWith(
                compareBy(
                    { (it.yCenter / errorValue).roundToInt()} ,{ (it.xCenter / errorValue).roundToInt() }
                )
            )
            val idx = (sai.labelPos-1).coerceAtMost(firFilter.size-1)
            target = firFilter[idx]
        }else{
            target = firFilter[0]
        }
        val rel = ocrRes.filter { item ->
            item.ocr!=null && when (direct){
                2 ->  (item.rect.x > target.rect.x - lossX && item.rect.x < target.rect.x ) &&
                        (item.rect.y > target.rect.y - lossY && item.rect.y < target.rect.y)
                5 -> if (lossX <= 0){
                    (item.rect.x > target.rect.x) && item.rect.y > target.rect.y - lossY && item.rect.y < target.rect.y + lossY
                } else
                    (item.rect.x > target.rect.x  && item.rect.x < target.rect.x + lossX) && item.rect.y > target.rect.y - lossY && item.rect.y < target.rect.y + lossY
                else -> false
            }
        }.sortedWith (
            compareBy(
                { (it.yCenter / errorValue).roundToInt()} ,{ (it.xCenter / errorValue).roundToInt() }
            )
        )
        print(rel.toString())
        if (rel.isEmpty()){
            return false
        }
        val idxT =  idx.coerceAtMost(firFilter.size-1)
        rel[idxT].ocr?.label?.let {
            if(rel.size > idx && it.containsAll(sai.txtFirstLab)){
                sai.point = getPoint(rel[idxT])
            }else{
                return false
            }
        }
        return true
    }
}