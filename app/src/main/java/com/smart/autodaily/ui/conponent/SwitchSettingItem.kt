package com.smart.autodaily.ui.conponent

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.smart.autodaily.data.entity.ScriptSetInfo

@Composable
fun SwitchSettingItem(
    setting: ScriptSetInfo,
    onSwitchChange: (ScriptSetInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    var isChecked by remember { mutableStateOf(
        if (setting.setValue!!.isBlank()) setting.setDefaultValue.toBoolean()
        else setting.setValue.toBoolean()
    ) }
    Row(modifier = Modifier.padding(all = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text = setting.setName, modifier = Modifier.weight(1f))
        Switch(checked = isChecked,
            onCheckedChange = {
                isChecked = it
                setting.setValue = it.toString()
                onSwitchChange(setting)
            })
    }
}