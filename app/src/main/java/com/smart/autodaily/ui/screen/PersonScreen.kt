package com.smart.autodaily.ui.screen

import android.annotation.SuppressLint
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.smart.autodaily.constant.BorderDirection
import com.smart.autodaily.constant.Ui
import com.smart.autodaily.ui.conponent.SingleBorderBox
import com.smart.autodaily.utils.ToastUtil
import com.smart.autodaily.utils.isLogin
import com.smart.autodaily.viewmodel.PersonViewModel
import kotlinx.coroutines.launch

@SuppressLint("DefaultLocale")
@Composable
fun PersonScreen(modifier: Modifier,
    nhc: NavHostController,
    personViewModel: PersonViewModel = viewModel()
) {
    //Text(text = "Hello PersonalScreen！")
    val user by  personViewModel.appViewModel.user.collectAsState()
    //是否开启弹窗
    val openDialog = remember { mutableStateOf(false) }
    //是否显示好友邀请码悬浮窗
    val inviteDialog = remember {
        mutableStateOf(false)
    }
    //激活码、邀请码
    val keyInfo = remember { mutableStateOf("") }
    //是否开启确认按钮
    var keyConfirmEnable by remember { mutableStateOf(true) }
    //登出后更新user
    Column {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

                /*Surface(color = Color.Yellow, modifier = Modifier.size(50.dp), shape = CircleShape) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                        Text(text = "Hello Word")
                    }
                }*/
            Column {
                user?.let {
                    Text(text = it.email)
                    TextCustomFirst( "邀请码："+it.inviteCode)
                    Text(
                        modifier = Modifier.clickable {
                                                      personViewModel.context.startActivity(
                                                          Intent("android.intent.action.COIN EXCHANGE").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                      )
                        },
                        fontSize = TextUnit(14f, TextUnitType.Sp),text = "AD币："+if(it.virtualCoin == null) 0 else String.format("%.2f",it.virtualCoin))
                    //TextCustomFirst()
                }
            }
            TextButton(
                onClick = {
                    if (user == null){
                        personViewModel.context.startActivity(
                            Intent("android.intent.action.LOGIN").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }else{
                        personViewModel.logout(user!!)
                    }
                }
            ) {
                user?.let {
                    Text(text = "退出>")
                } ?: Text(text = "登录>")
            }
        }
        
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
            PersonColumnFirst("单次运行",(user?.canRunNum?:"--").toString() + "个")
            SingleBorderBox(
                modifier = Modifier
                    .width(1.dp)
                    .height(24.dp),
                direction = BorderDirection.RIGHT,
                content = {}
            )
            PersonColumnFirst("到期", user?.expirationTime?:"--")
        }

        PersonRowFirst(textLabel = "兑换码", imageVector = Icons.Outlined.ShoppingCart){
            if(isLogin(personViewModel.context, user)){
                openDialog.value = true
            }
        }
        user?.let {
            if(it.inviteCodeFather.isNullOrBlank()){
                PersonRowFirst(textLabel = "输入好友邀请码", imageVector = Icons.Outlined.AccountCircle){
                    inviteDialog.value = true
                }
            }
        }
        PersonRowFirst(textLabel = "分享", imageVector = Icons.Outlined.Share){
            isLogin(personViewModel.context, user)
        }
        PersonRowFirst(textLabel = "检查更新", imageVector = Icons.Outlined.Refresh){

        }
        PersonRowFirst(textLabel = "反馈", imageVector = Icons.Outlined.Create){
            isLogin(personViewModel.context, user)
        }
        PersonRowFirst(textLabel = "关于", imageVector = Icons.Outlined.Info){

        }
    }
    if (openDialog.value){
        keyInfo.value = ""
        keyConfirmEnable = true
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
                    text = "请输入您的兑换码",
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

    if (inviteDialog.value){
        keyInfo.value = ""
        keyConfirmEnable = true
        AlertDialog(
            //properties = DialogProperties(),
            onDismissRequest = {
                inviteDialog.value = false
            },
            confirmButton = {
                OutlinedButton(
                    enabled = keyConfirmEnable,
                    onClick = {
                        keyConfirmEnable = false
                        personViewModel.viewModelScope.launch {
                            val result = personViewModel.inputInvitorCode(user!!.userId,keyInfo.value)
                            if (result.code == 200){
                                inviteDialog.value = false
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
                    text = "输入好友邀请码，得vip",
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
    onClick :  ()->Unit
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
            Spacer(modifier = Modifier.width(Ui.SPACE_8))
            Text(text = textLabel)
        }

        Text(text = ">")
    }
}

