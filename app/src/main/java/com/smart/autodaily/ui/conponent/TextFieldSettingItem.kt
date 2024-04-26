package com.smart.autodaily.ui.conponent

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.smart.autodaily.data.entity.ScriptSetInfo

@Composable
fun TextFieldSettingItem(
    setting : ScriptSetInfo,
    modifier: Modifier = Modifier,
    onValueChange: (setting: ScriptSetInfo) -> Unit
){
    Row {
        Text(text = setting.set_name)
        OutlinedTextField(
            value = if (setting.set_default_value.isEmpty()) setting.set_default_value else setting.set_value,
            onValueChange = {
                setting.set_value = it
                onValueChange(setting)
            },
        )
    }
}