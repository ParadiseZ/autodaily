package com.smart.autodaily.data.entity

data class ConfigData(
    //截图前延迟
    val intervalTime : Long,
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

    //当前记忆的系统时间
    var remRebootTime : Long,
    //循环截图识别标志
    //var loopFlag: Boolean,
    //重试延迟
    val retryDelay : Long,
    var remRetryTime : Long,
    //下次是否需要尝试返回操作
    var isTryBack : Boolean,
    val remTryBackLabel : MutableList<Int>,
    val maxRetryNum : Int,
    var curRetryNum : Int,


    var beforeClickIdx : Int,
    var beforePoint : Point
){
    var beforeHash : ByteArray = ByteArray(0)
    var curHash : ByteArray = ByteArray(0)
}
