package com.smart.autodaily.ui.conponent

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import com.smart.autodaily.constant.ScreenText

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
                modifier = Modifier.fillMaxWidth()
                    .focusRequester(focusRequester), // 放入FocusRequester实例以便获取焦点
                shape = RoundedCornerShape(50),
                singleLine = true,
                value = textValue,
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Search, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),//替换回车键为搜索图标
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