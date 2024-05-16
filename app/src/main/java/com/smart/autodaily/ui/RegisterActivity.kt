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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smart.autodaily.ui.conponent.LockScreenLoading
import com.smart.autodaily.utils.ToastUtil
import com.smart.autodaily.utils.ValidUtil
import com.smart.autodaily.viewmodel.RegisterViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Theme wrappers and other composables may be needed based on your application setup
            RegisterScreen()
        }
    }
}

@Composable
fun RegisterScreen() {
    val registerViewModel : RegisterViewModel = viewModel()
    // You should use proper state-hoisting for real-world scenarios
    var username by remember { mutableStateOf("") }
    var emailCheckCode by remember { mutableStateOf("") }
    var emailCheckButtonEnabled by remember { mutableStateOf(true) }
    var waitTime by remember { mutableIntStateOf(0) }
    var password by remember { mutableStateOf("") }
    var inviteCodeFather by remember { mutableStateOf("") }
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
                Text(text = "注册AutoDaily", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("邮箱") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ){
                    OutlinedTextField(
                        value = emailCheckCode,
                        onValueChange = { emailCheckCode = it },
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
                            if(registerCheck(username,registerViewModel.context)){
                                emailCheckButtonEnabled = false
                                registerViewModel.viewModelScope.launch {
                                    val result = registerViewModel.sendEmailCode(username, 0)
                                    if (result.code == 200){
                                        for (i in 1..60){
                                            waitTime = 60 - i
                                            delay(1000)
                                        }
                                    }else{
                                        ToastUtil.show(registerViewModel.context,result.message.toString())
                                    }
                                    emailCheckButtonEnabled = true
                                }
                            }
                        },
                        modifier = Modifier.weight(1.5f).alignByBaseline()
                    ) {
                        if(waitTime == 0){
                            Text(text = "验证码")
                        }else{
                            Text(text = waitTime.toString())
                        }

                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("密码") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = inviteCodeFather,
                    onValueChange = { inviteCodeFather = it },
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
                            registerViewModel.context.startActivity(
                                Intent("android.intent.action.LOGIN")
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "< 返回登录")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if(registerCheck(username, password, registerViewModel.context)){
                                registerViewModel.viewModelScope.launch {
                                    isLocked.value = true
                                    val registerResult = registerViewModel.registerByEmail(username,emailCheckCode, password, inviteCodeFather)
                                    isLocked.value = false
                                    registerResult.message?.let {
                                        ToastUtil.show(registerViewModel.context,
                                            it
                                        )
                                    }
                                    if (registerResult.code == 200){
                                        ToastUtil.show(registerViewModel.context, "注册成功！")
                                        registerViewModel.context.startActivity(
                                            Intent("android.intent.action.LOGIN")
                                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        )
                                    }
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

private fun registerCheck(username: String, password: String,context: Context):Boolean{
    if( username.isEmpty() || password.isEmpty() ) {
        ToastUtil.show(context, "邮箱或密码不能为空")
        return false
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

private fun registerCheck(username: String,context: Context):Boolean{
    if( username.isEmpty()) {
        ToastUtil.show(context, "邮箱不能为空")
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