package com.smart.autodaily.ui.conponent

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.smart.autodaily.constant.Ui
import com.smart.autodaily.data.entity.ScriptSetInfo

@Composable
fun SliderSettingItem(
    setting: ScriptSetInfo,
    onSliderValueChange: (ScriptSetInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    var sliderValue by remember { mutableFloatStateOf(
        if (setting.setValue?.isNotBlank() == true) setting.setValue!!.toFloat() else setting.setDefaultValue?.toFloat() ?:0f
    ) }
    Box(modifier = modifier
        .wrapContentSize()
        .padding(Ui.SPACE_4)
        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), shape = RoundedCornerShape(5.dp))
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(modifier = Modifier.padding(start = Ui.SPACE_4),text = setting.setName+"：")
            Text(text = "${"%.0f".format(sliderValue * 100)}%")
            Slider(value = sliderValue, onValueChange = {
                sliderValue = it
                setting.setValue = it.toString()
                onSliderValueChange(setting)
                },
                colors = SliderDefaults.colors(
                    thumbColor = Color.White, // 圆圈的颜色
                    activeTrackColor = Color(0xFF0079D3)
                )
            )
        }
    }
}