package com.smart.autodaily.handler

import android.accessibilityservice.GestureDescription
import android.graphics.Path
import com.smart.autodaily.data.entity.Point
import com.smart.autodaily.utils.ScreenCaptureUtil

object ActionHandler {
    private val path  = Path()
    private val clickTime = 1L
    private val longClickTime = 500L
    private var randomXRange = 5f
    private var randomYRange = 5f

    fun updateRandomRange(xRange : Float, yRange: Float){
        this.randomXRange = xRange
        this.randomYRange = yRange
    }

    fun click(point: Point){
        clickGesture(point, 1)
    }

    fun longClick(point: Point){
        clickGesture(point, 2)
    }

    private fun clickGesture(point: Point, type: Int){
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
        ScreenCaptureUtil.accessibilityService?.dispatchGesture(
            gestureDescription!!, null, null
        )
    }
}