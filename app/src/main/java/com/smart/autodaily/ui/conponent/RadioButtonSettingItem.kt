package com.smart.autodaily.ui.conponent

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
fun RadioButtonSettingItem(
    setting: ScriptSetInfo,
    onCheckedChange: (ScriptSetInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    var setValue by remember {
        mutableStateOf(setting.setValue)
    }
    Box(modifier = modifier
        .wrapContentSize()
        .padding(Ui.SPACE_4)
        .border(
            1.dp,
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            shape = RoundedCornerShape(5.dp)
        )
    ) {
        Column(
            modifier = modifier.fillMaxWidth(),
        ) {
            Text(modifier = Modifier.padding(start = Ui.SPACE_4),text = setting.setName)
            Row (
                modifier = modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ){
                setting.setDefaultValue.split(",").forEach {
                    RadioButton(
                        selected = it == setValue,
                        onClick = {
                            setValue = it
                            setting.setValue = it
                            onCheckedChange(setting)
                        }
                    )
                    Text(modifier = Modifier.clickable{
                        setValue = it
                        setting.setValue = it
                        onCheckedChange(setting)
                    },text = it)
                }
            }
        }

    }
}