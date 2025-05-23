package com.smart.autodaily.data.entity

import android.graphics.Bitmap
import com.smart.autodaily.command.CommandExecutor

data class ConfigData(
    //截图前延迟
    val intervalTime : Long,
    //是否记录完成情况
    val recordStatus : Boolean,
    //置信度分数过滤
    val similarScore : Float,
    //重启时间
    val rebootDelay : Long,
    //是否使用GPU
    val useGpu : Boolean,
    //是否尝试返回操作
    val tryBackAction : Boolean,
    //随机点击的范围
    val random: Float,
    //OCR缩放倍数
    val detectSize : Int,
    //当前记忆的系统时间
    var remRebootTime : Long,
    //循环截图识别标志
    //var loopFlag: Boolean,
    //重试延迟
    val retryDelay : Long,
    //最大重试次数
    val maxRetryNum : Int,
    val minScreen : Int,
    var executor : CommandExecutor?
){
    var beforeHash : ByteArray = byteArrayOf()
    var pkgName = ""
    var capture : Bitmap?=null
    var capScale : Int = 1
}
