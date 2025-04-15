package com.smart.autodaily.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.smart.autodaily.constant.Screen
import com.smart.autodaily.ui.conponent.LockScreenLoading
import com.smart.autodaily.ui.navigation.navSingleTopTo
import com.smart.autodaily.utils.ValidUtil
import com.smart.autodaily.viewmodel.RegisterViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(modifier: Modifier,navController: NavController) {
    val registerViewModel : RegisterViewModel = viewModel()
    // You should use proper state-hoisting for real-world scenarios
    var username by remember { mutableStateOf("") }
    var emailCheckCode by remember { mutableStateOf("") }
    var emailCheckButtonEnabled by remember { mutableStateOf(true) }
    var waitTime by remember { mutableIntStateOf(0) }
    var password by remember { mutableStateOf("") }
    var inviteCodeFather by remember { mutableStateOf("") }
    val isLocked = remember { mutableStateOf(false) }
    val notification = remember { mutableStateOf("") }

    LockScreenLoading(
        isLocked =isLocked,
        content = {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "注册AutoDaily", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it
                        notification.value = ""},
                    label = { Text("邮箱") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (notification.value.isNotBlank()){
                    Text(modifier = Modifier.fillMaxWidth(),text = notification.value, color = Color.Red)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Row (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    OutlinedTextField(
                        value = emailCheckCode,
                        onValueChange = { emailCheckCode = it
                            notification.value = ""},
                        label = { Text("验证码") },
                        modifier = Modifier.fillMaxWidth()
                            .weight(3f)
                            .alignByBaseline(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        enabled = emailCheckButtonEnabled,
                        onClick = {
                            if (username.isEmpty() || !ValidUtil.isValidEmail(username)){
                                notification.value = "邮箱不符合规范"
                                return@Button
                            }
                            emailCheckButtonEnabled = false
                            registerViewModel.viewModelScope.launch {
                                val result = registerViewModel.sendEmailCode(username, 0)
                                if (result.code == 200){
                                    for (i in 1..60){
                                        waitTime = 60 - i
                                        delay(1000)
                                    }
                                }else{
                                    result.message?.let {
                                        notification.value = it
                                    }
                                }
                                emailCheckButtonEnabled = true
                            }
                        },
                        modifier = Modifier.weight(1.5f).alignByBaseline()
                    ) {
                        if(waitTime == 0){
                            Text(text = "获取")
                        }else{
                            Text(text = waitTime.toString())
                        }

                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it
                        notification.value = ""},
                    label = { Text("密码") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = inviteCodeFather,
                    onValueChange = { inviteCodeFather = it
                        notification.value = ""},
                    label = { Text("邀请码(选填)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ){
                    Button(
                        onClick = {
                            navController.navSingleTopTo(Screen.LOGIN.name)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "< 返回登录")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            notification.value = ""
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
                            if (!ValidUtil.isValidEmailCode(emailCheckCode)){
                                notification.value = "验证码不符合规范（6位）"
                                return@Button
                            }
                            registerViewModel.viewModelScope.launch {
                                isLocked.value = true
                                val registerResult = registerViewModel.registerByEmail(username,emailCheckCode, password, inviteCodeFather)
                                isLocked.value = false
                                registerResult.message?.let {
                                    notification.value = it
                                }
                                if (registerResult.code == 200){
                                    notification.value = "注册成功！"
                                    delay(1000)
                                    navController.navSingleTopTo(Screen.LOGIN.name)
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "注册")
                    }
                }

            }
        }
    )

}