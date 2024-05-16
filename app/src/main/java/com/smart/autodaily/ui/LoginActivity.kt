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
import androidx.compose.material3.TextButton
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
import com.smart.autodaily.MainActivity
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
                                    if (loginResult.code==200){
                                        loginViewMode.context.startActivity(
                                            Intent(loginViewMode.context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        )
                                    }else{
                                        loginResult.message?.let {
                                            ToastUtil.show(loginViewMode.context,
                                                it
                                            )
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        loginViewMode.context.startActivity(
                            Intent("android.intent.action.ResetPassword").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }) {
                        Text(text = "忘记密码？")
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