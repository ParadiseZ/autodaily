package com.smart.autodaily.ui.conponent

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.smart.autodaily.constant.BorderDirection

@Composable
fun SingleBorderBox(
    modifier: Modifier = Modifier,
    direction : BorderDirection,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .drawBehind {
                val strokeWidth = 1.dp.toPx() // 边框宽度，之举可据需调整
                var start :Offset ?=null
                var end :Offset ?=null
                when(direction){
                    BorderDirection.TOP  -> {
                        start =  Offset(0f,  strokeWidth / 2)
                        end = Offset(size.width, strokeWidth / 2)
                    }
                    BorderDirection.BOTTOM ->{
                        start = Offset(0f, size.height - strokeWidth / 2)
                        end =  Offset(size.width, size.height - strokeWidth / 2)

                    }
                    BorderDirection.LEFT ->{
                        start = Offset(0f, strokeWidth / 2)
                        end = Offset(0f, size.height - strokeWidth / 2)
                    }
                    BorderDirection.RIGHT->{
                        start = Offset(size.width, strokeWidth / 2)
                        end = Offset(size.width, size.height - strokeWidth / 2)
                    }
                    BorderDirection.NULL ->{
                        start = Offset(0f,0f)
                        end = Offset(0f,0f)
                    }
                }
                drawLine(
                    color = androidx.compose.ui.graphics.Color.Gray.copy(0.5f),
                    start = start,
                    end = end,
                    strokeWidth = strokeWidth
                )
            }
    ) {
        content() // 内容物展示部分
    }
}