package com.smart.autodaily.ui.conponent

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.smart.autodaily.data.entity.ScriptSetInfo

@Composable
fun SliderSettingItem(
    setting: ScriptSetInfo,
    onSliderValueChange: (ScriptSetInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    var sliderValue by remember { mutableFloatStateOf(
        if (setting.set_value.isNotBlank()) setting.set_value.toFloat() else setting.set_default_value.toFloat()
    ) }
    Row(modifier = modifier.padding(all = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text = setting.set_name, modifier = modifier.weight(1f))
        Column(modifier = modifier.padding(all = 8.dp)) {
            Slider(value = sliderValue, onValueChange = {
                sliderValue = it
                onSliderValueChange(setting)
            })
            Text(text = "${sliderValue}%")
        }
    }
}