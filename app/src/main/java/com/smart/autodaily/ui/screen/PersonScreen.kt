package com.smart.autodaily.ui.screen

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.smart.autodaily.constant.BorderDirection
import com.smart.autodaily.data.entity.UserInfo
import com.smart.autodaily.ui.conponent.SingleBorderBox
import com.smart.autodaily.utils.ToastUtil
import com.smart.autodaily.viewmodel.PersonViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonScreen(modifier: Modifier,
                   nhc: NavHostController,
                   personViewModel: PersonViewModel = viewModel()
) {
    //Text(text = "Hello PersonalScreen！")
    var user by  remember{
        mutableStateOf<UserInfo?>(null)
    }
    //是否开启弹窗
    val openDialog = remember { mutableStateOf(false) }
    //激活码
    val keyInfo = remember { mutableStateOf("") }
    //是否开启确认按钮
    var keyConfirmEnable by remember { mutableStateOf(true) }
    //登出后更新user
    val logout = remember { mutableStateOf(false) }
    LaunchedEffect(key1 = logout.value) {
        user =  personViewModel.getUserInfoLocal()
    }
    Column{
        SingleBorderBox(
            modifier = Modifier.padding(8.dp),
            direction = BorderDirection.BOTTOM
        ) {
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){
                Surface(color = Color.Yellow, modifier = Modifier.size(50.dp), shape = CircleShape) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                        Text(text = "Hello Word")
                    }
                }
                Column {
                    user?.let {
                        Text(text = it.email)
                        TextCustomFirst( "邀请码："+it.inviteCode)
                        TextCustomFirst("A币："+it.password)
                    }
                }
                TextButton(
                    onClick = {
                        if (user == null){
                            personViewModel.context.startActivity(
                                Intent("android.intent.action.LOGIN").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }else{
                            personViewModel.viewModelScope.launch {
                                personViewModel.logout(user!!)
                                logout.value = true
                            }
                        }
                    }
                ) {
                    user?.let {
                        Text(text = "退出>")
                    } ?: let{
                        Text(text = "登录>")
                    }
                }
            }
        }


        Spacer(modifier = Modifier.height(8.dp))
        
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ){
            PersonColumnFirst("类型",user?.keyTypeName?:"--")
            SingleBorderBox(
                modifier = Modifier
                    .width(1.dp)
                    .height(24.dp),
                direction = BorderDirection.RIGHT,
                content = {}
            )
            PersonColumnFirst("剩余",(user?.canActivateNum?:"--").toString() + "个")
            SingleBorderBox(
                modifier = Modifier
                    .width(1.dp)
                    .height(24.dp),
                direction = BorderDirection.RIGHT,
                content = {}
            )
            PersonColumnFirst("到期", user?.registerTime?:"--")
        }

        PersonRowFirst(textLabel = "兑换码", imageVector = Icons.Default.ShoppingCart){
            if (user == null){
                personViewModel.context.startActivity(
                    Intent("android.intent.action.LOGIN").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }else{
                openDialog.value = !openDialog.value
            }
        }

        PersonRowFirst(textLabel = "输入好友邀请码", imageVector = Icons.Default.AccountCircle){

        }
        PersonRowFirst(textLabel = "分享", imageVector = Icons.Default.Share){

        }
        PersonRowFirst(textLabel = "检查更新", imageVector = Icons.Default.Refresh){

        }
        PersonRowFirst(textLabel = "反馈", imageVector = Icons.Default.Create){

        }
        PersonRowFirst(textLabel = "关于", imageVector = Icons.Default.Info){

        }
    }
    if (openDialog.value){
        AlertDialog(
            //properties = DialogProperties(),
            onDismissRequest = {
                openDialog.value = false
            },
            confirmButton = {
                OutlinedButton(
                    enabled = keyConfirmEnable,
                    onClick = {
                        keyConfirmEnable = false
                        personViewModel.viewModelScope.launch {
                            val result = personViewModel.inputKey(user!!.userId,keyInfo.value)
                            if (result.code == 200){
                                openDialog.value = false
                                keyConfirmEnable = true
                            }
                            result.data?.let {
                                user = it
                            }
                            result.message?.let {
                                ToastUtil.show(personViewModel.context,
                                    it
                                )
                            }
                            keyConfirmEnable = true
                        }
                    }
                ){
                    Text(text = "确定")
                }
            },
            title = {
                Text(
                    text = "输入兑换码",
                    fontWeight = FontWeight.W700,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            text = {
                TextField(value = keyInfo.value, onValueChange ={
                    keyInfo.value = it
                })
            },
        )
    }
}

@Composable
fun PersonColumnFirst(
    textLabel : String,
    textValue : String,
){
    Column (
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(text = textLabel)
        TextCustomFirst(textValue)
    }
}

@Composable
fun TextCustomFirst(
    value : String
){
    Text(fontSize = TextUnit(12f, TextUnitType.Sp),text = value)
}

@Composable
fun PersonRowFirst(
    textLabel: String,
    imageVector: ImageVector,
    onClick : ()->Unit
){
    Spacer(modifier = Modifier.height(8.dp))
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                onClick()
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ){
        Row(
            verticalAlignment = Alignment.CenterVertically
        ){
            Icon(imageVector = imageVector, contentDescription = null)
            Text(text = textLabel)
        }

        Text(text = "＞")
    }
}