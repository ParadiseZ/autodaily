package com.smart.autodaily.ui.conponent

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.smart.autodaily.constant.Ui
import com.smart.autodaily.data.entity.ScriptSetInfo

@Composable
fun CheckBoxSettingItem(
    setting: ScriptSetInfo,
    onCheckedChange: (setting: ScriptSetInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    var isChecked by remember {
        mutableStateOf(
            setting.checkedFlag
        )
    }
    Box(modifier = modifier
        .wrapContentSize()
        .padding(Ui.SPACE_4)
        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), shape = RoundedCornerShape(5.dp))
        .clickable {
            isChecked = !isChecked
            setting.checkedFlag = isChecked
            setting.setValue = if(isChecked) "true" else "false"
            onCheckedChange(setting)
        }
    ){
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ){
            Checkbox(checked = isChecked, onCheckedChange = {
                isChecked = it
                setting.checkedFlag = it
                setting.setValue = if(isChecked) "true" else "false"
                onCheckedChange(setting)
            })
            Text(text = setting.setName)
            Spacer(modifier = Modifier.width(Ui.SPACE_4))
        }
    }

}