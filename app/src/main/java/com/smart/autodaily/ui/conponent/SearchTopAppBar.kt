package com.smart.autodaily.ui.conponent

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.smart.autodaily.constant.Ui

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopAppBar(
    searchButtonText: String,
    onSearchClick: (String) -> Unit
) {
    var textValue by remember {
        mutableStateOf("")
    }//文本内容记录
    val focusManager = LocalFocusManager.current    //焦点管理器
    val focusRequester = remember { FocusRequester() }  // 创造一个FocusRequester实例
    TopAppBar(
        title = {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)// 放入FocusRequester实例以便获取焦点
                    .defaultMinSize(
                        minWidth = OutlinedTextFieldDefaults.MinWidth,
                        minHeight = OutlinedTextFieldDefaults.MinHeight / 10 * 9
                    )
                    .padding(top = 4.dp),//OutlinedTextFieldTopPadding/2
                shape = RoundedCornerShape(50),
                textStyle = LocalTextStyle.current.copy(color = LocalTextStyle.current.color, fontSize = Ui.SIZE_14),
                singleLine = true,
                value = textValue,
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Search, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),//替换回车键为搜索图标,
                keyboardActions = KeyboardActions(
                    onSearch = {
                        defaultKeyboardAction(ImeAction.Done)   //隐藏键盘
                        focusManager.clearFocus()   //清除焦点
                        onSearchClick(textValue)    //执行搜索
                    },
                ),
                trailingIcon = {
                    if (textValue.isNotEmpty()) {   //不为空时显示清除按钮
                        IconButton(onClick = {
                            textValue = ""
                            focusRequester.requestFocus()   //OutlinedTextField获取焦点
                            onSearchClick(textValue) //需要执行搜索，
                        }) {
                            Icon(imageVector = Icons.Filled.Clear, contentDescription = null)
                        }
                    }
                },
                onValueChange = {
                    textValue = it
                }
            )
        },
        actions = {
            TextButton(onClick = {
                onSearchClick(textValue)
            }) {
                Text(text = searchButtonText)
            }
        },
    )
}