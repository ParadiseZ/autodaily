package com.smart.autodaily.ui.conponent

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.smart.autodaily.data.entity.ScriptSetInfo

@Composable
fun TextFieldSettingItem(
    setting : ScriptSetInfo,
    modifier: Modifier = Modifier,
    onValueChange: (setting: ScriptSetInfo) -> Unit
){
    var textInput by remember {
        mutableStateOf(
            if (setting.setValue!!.isBlank()) setting.setDefaultValue else setting.setValue
        )
    }
    var isError by rememberSaveable { mutableStateOf(false) }

    fun validate(text: String) {
        isError = text.count() < 5
    }
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = setting.setName)
        OutlinedTextField(
            modifier = Modifier
                .semantics {
                    if (isError) error("Email format is invalid.")
                }
                .wrapContentSize()
                .padding(4.dp),
            value = textInput!!,
            maxLines = 1,
            onValueChange = {
                isError = false
                setting.setValue = it
                textInput = it
                onValueChange(setting)
            },
            keyboardActions = KeyboardActions(onDone ={
                validate(textInput!!)
            })
            /*KeyboardActions {
                validate(textInput)
            }*/
        )
    }
}