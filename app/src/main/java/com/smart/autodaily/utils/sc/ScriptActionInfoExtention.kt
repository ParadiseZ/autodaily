package com.smart.autodaily.utils.sc

import com.jeremyliao.liveeventbus.LiveEventBus
import com.smart.autodaily.data.entity.Point
import com.smart.autodaily.data.entity.ScriptActionInfo


fun ScriptActionInfo.AsClick(point: Point? = null) : ScriptActionInfo{
    if (point == null){
        LiveEventBus.get<Point>("click").post(this.point)
    }else{
        LiveEventBus.get<Point>("click").post(point)
    }
    return this
}

fun ScriptActionInfo.AsLongClick(point: Point?= null) : ScriptActionInfo{
    if (point == null){
        LiveEventBus.get<Point>("longClick").post(this.point)
    }else{
        LiveEventBus.get<Point>("longClick").post(point)
    }
    return this
}