package com.smart.autodaily.handler

import android.accessibilityservice.GestureDescription
import android.graphics.Path
import com.smart.autodaily.data.entity.Point
import com.smart.autodaily.service.AssService

object ActionHandler {
    private val path  = Path()
    private val clickTime = 1L
    private val longClickTime = 500L
    var randomXRange = 0f
    var randomYRange = 0f

    fun updateRandomRange(xRange : Float, yRange: Float){
        this.randomXRange = xRange
        this.randomYRange = yRange
    }

    fun click(point: Point, assService: AssService){
        clickGesture(point, 1, assService)
    }

    fun longClick(point: Point, assService: AssService){
        clickGesture(point, 2, assService)
    }

    private fun clickGesture(point: Point, type: Int, assService: AssService){
        path.moveTo(    point.x+ randomXRange , point.y + randomYRange  )
        var gestureDescription : GestureDescription?=null
        when(type){
            1 ->gestureDescription = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, clickTime))
                .build()
            2 ->gestureDescription = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, longClickTime))
                .build()
        }
        assService.dispatchGesture(
            gestureDescription!!, null, null
        )
    }
}