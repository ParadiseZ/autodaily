package com.smart.autodaily.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smart.autodaily.constant.LoginResult
import com.smart.autodaily.ui.conponent.LockScreenLoading
import com.smart.autodaily.utils.ToastUtil
import com.smart.autodaily.utils.ValidUtil
import com.smart.autodaily.viewmodel.LoginViewModel
import kotlinx.coroutines.launch

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoginScreen()
        }
    }
}

@Composable
fun LoginScreen() {
    val loginViewMode : LoginViewModel = viewModel()
    // You should use proper state-hoisting for real-world scenarios
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val isLocked = remember { mutableStateOf(false) }
    LockScreenLoading(
        isLocked =isLocked,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "登录AutoDaily", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("邮箱") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("密码") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ){
                    Button(
                        onClick = {
                            loginViewMode.context.startActivity(
                                Intent("android.intent.action.REGISTER").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "注册")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if(loginCheck(username, password, loginViewMode.context)){
                                loginViewMode.viewModelScope.launch {
                                    isLocked.value = true
                                    val loginResult = loginViewMode.loginByEmail(username, password)
                                    isLocked.value = false
                                    when(loginResult){
                                        LoginResult.LOGIN_SUCCESS->{
                                            loginViewMode.context.startActivity(
                                                Intent("android.intent.action.MAIN").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            )
                                        }
                                        LoginResult.LOGIN_FAILED->{
                                            ToastUtil.show(loginViewMode.context, "登录失败，请检查邮箱或密码是否正确")
                                        }
                                        LoginResult.NETWORK_ERROR->{
                                            ToastUtil.show(loginViewMode.context, "连接网络超时，请稍后重试")
                                        }
                                        LoginResult.UNKNOWN_ERROR -> {
                                            ToastUtil.show(loginViewMode.context, "未知异常，请稍后重试")
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "登录")
                    }
                }
            }
        }
    )
}

private fun loginCheck(username: String, password: String,context: Context):Boolean{
    if( username.isEmpty() || password.isEmpty() ) {
        ToastUtil.show(context, "邮箱或密码不能为空")
    }
    if ( ValidUtil.isValidEmail(username) ){
        return true
    }else{
        if( ValidUtil.isNumeric(username) ){
            return true
        }else{
            ToastUtil.show(context, "邮箱不符合规范")
        }
    }
    return false
}