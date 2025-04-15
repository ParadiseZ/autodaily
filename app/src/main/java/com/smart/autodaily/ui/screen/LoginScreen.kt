package com.smart.autodaily.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.smart.autodaily.constant.ResponseCode
import com.smart.autodaily.constant.Screen
import com.smart.autodaily.ui.conponent.LockScreenLoading
import com.smart.autodaily.ui.navigation.navSingleTopTo
import com.smart.autodaily.utils.ValidUtil
import com.smart.autodaily.viewmodel.LoginViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    modifier: Modifier,
    navController : NavController
) {
    val loginViewMode : LoginViewModel = viewModel()
    // You should use proper state-hoisting for real-world scenarios
    var acceptPrivacy by rememberSaveable { mutableStateOf(false) }
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val isLocked = remember { mutableStateOf(false) }
    val notification = remember { mutableStateOf("") }
    val privacy = buildAnnotatedString {
        //append("我已阅读并同意")
        pushStringAnnotation(
            tag = "PRIVACY",
            annotation = "PRIVACY"
        )
        withStyle(
            style = SpanStyle(
                color = Color(0xFF0E9FF2),
                fontWeight = FontWeight.Bold,

                )
        ) {
            append("《隐私政策》")
        }
        pop()
    }
    val termsUse = buildAnnotatedString {
        //append("我已阅读并同意")
        pushStringAnnotation(
            tag = "TERMS_OF_USE",
            annotation = "TERMS_OF_USE"
        )
        withStyle(
            style = SpanStyle(
                color = Color(0xFF0E9FF2),
                fontWeight = FontWeight.Bold
            )
        ) {
            append("《使用条款》")
        }
        pop()
    }
    LockScreenLoading(
        isLocked =isLocked,
        content = {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "登录AutoDaily", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        notification.value = "" },
                    label = { Text("邮箱") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))
                if (notification.value.isNotBlank()){
                    Text(modifier = Modifier.fillMaxWidth(),text = notification.value, color = Color.Red)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it
                        notification.value = "" },
                    label = { Text("密码") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .clickable{
                            acceptPrivacy = !acceptPrivacy
                            notification.value = ""
                        },
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Checkbox(
                        checked = acceptPrivacy,
                        onCheckedChange = {
                            acceptPrivacy = it
                            notification.value = ""},
                    )
                    Text(text = "我已阅读并同意")
                    Text(
                        modifier = Modifier
                            .clickable{
                                val data = "PRIVACY"
                                navController.navigate(Screen.LICENSESHOW.name + "/$data")
                            },
                        text = privacy,
                        style = TextStyle(textDecoration = TextDecoration.Underline),
                    )
                    Text(text = "以及")
                    Text(
                        modifier = Modifier
                            .clickable{
                                val data = "TERMS_OF_USE"
                                navController.navigate(Screen.LICENSESHOW.name + "/$data")
                            },
                        text = termsUse,
                        style = TextStyle(textDecoration = TextDecoration.Underline),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ){
                    Button(
                        onClick = {
                            navController.navSingleTopTo(Screen.REGISTER.name)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "注册")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            notification.value = ""
                            if(!acceptPrivacy){
                                notification.value = "请先阅读并同意隐私政策"
                                return@Button
                            }
                            if(username.isEmpty() || password.isEmpty()){
                                notification.value = "邮箱或密码不能为空"
                                return@Button
                            }
                            if (!ValidUtil.isValidEmail(username)){
                                notification.value = "邮箱不符合规范"
                                return@Button
                            }
                            if (!ValidUtil.isValidPassword(password)){
                                notification.value = "密码不符合规范（8位以上非空字符）"
                                return@Button
                            }
                            loginViewMode.viewModelScope.launch {
                                isLocked.value = true
                                val loginResult = loginViewMode.loginByEmail(username, password)
                                isLocked.value = false
                                if (loginResult.code== ResponseCode.SUCCESS.code){
                                    navController.navSingleTopTo(Screen.HOME.name)
                                }else{
                                    loginResult.message?.let {
                                        notification.value = it
                                    }
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "登录")
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        navController.navSingleTopTo(Screen.RESETPWD.name)
                    }) {
                        Text(text = "忘记密码？")
                    }
                }
            }
        }
    )
}