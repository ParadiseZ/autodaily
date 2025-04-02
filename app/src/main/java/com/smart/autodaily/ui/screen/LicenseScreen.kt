package com.smart.autodaily.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.smart.autodaily.constant.NavigationItem
import com.smart.autodaily.utils.hasNotificationPermission
import com.smart.autodaily.viewmodel.LicenseViewModel
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LicenseScreen(
    navController: NavHostController,
    viewModel : LicenseViewModel = viewModel()
){
    var target="";
    var privacyChecked by remember {
        mutableStateOf(false)
    }
    var termsUseChecked by remember {
        mutableStateOf(false)
    }
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
    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.LightGray)
        .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ){
        Surface(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .background(Color.White)
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Column{
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = "欢迎使用AutoDaily")
                }
                Text(text = "本应用在使用时需要建立数据连接，期间会产生流量，流量费用由运营商收取。")
                Spacer(modifier = Modifier.height(5.dp))
                FlowRow (
                    modifier = Modifier.wrapContentSize(),
                    verticalArrangement = Arrangement.Center
                ){
                    Text(text = "在使用本产品前，请仔细阅读")
                    Text(
                        modifier = Modifier
                            .clickable{
                                val data = "PRIVACY"
                                navController.navigate(NavigationItem.LICENSESHOW.route + "/$data")
                            },
                        text = privacy,
                        style = TextStyle(textDecoration = TextDecoration.Underline),
                    )
                    Text(text = "以及")
                    Text(
                        modifier = Modifier
                            .clickable{
                                val data = "TERMS_OF_USE"
                                navController.navigate(NavigationItem.LICENSESHOW.route + "/$data")
                            },
                        text = termsUse,
                        style = TextStyle(textDecoration = TextDecoration.Underline),
                    )
                    Text(text = "。")
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { privacyChecked = !privacyChecked },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked =privacyChecked , onCheckedChange = {
                        privacyChecked = it

                    })
                    Text(text = "我已阅读并同意")
                    Text(
                        modifier = Modifier.clickable{
                            val data = "PRIVACY"
                            navController.navigate(NavigationItem.LICENSESHOW.route + "/$data")
                        },
                        text = privacy,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Row (
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { termsUseChecked = !termsUseChecked },
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Checkbox(checked =termsUseChecked , onCheckedChange = {
                        termsUseChecked = it
                    })
                    Text(text = "我已阅读并同意")
                    Text(
                        modifier = Modifier.clickable{
                            val data = "TERMS_OF_USE"
                            navController.navigate(NavigationItem.LICENSESHOW.route + "/$data")
                        },
                        text = termsUse,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row (
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ){
                    TextButton(onClick = {
                        exitProcess(0)
                    }) {
                        Text(text = "拒绝", color = Color(0xFF0E9FF2))
                    }
                    if(privacyChecked && termsUseChecked){
                        TextButton(onClick = {
                            viewModel.viewModelScope.launch {
                                viewModel.putPrivacyRes("PRIVACY",privacyChecked)
                                viewModel.putPrivacyRes("TERMS_OF_USE",termsUseChecked)
                                viewModel.getPrivacyRes()
                                if (!hasNotificationPermission()){
                                    target = NavigationItem.NOTIFICATION.route
                                }else{
                                    target = NavigationItem.HOME.route
                                }
                                navController.navigate(target){
                                    popUpTo(NavigationItem.LICENSE.route) { inclusive = true }
                                }
                            }
                            //viewModel.context.startActivity(Intent("android.intent.action.HOME").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        }) {
                            Text(text = "同意", color = Color(0xFF0E9FF2))
                        }
                    }else{
                        TextButton(onClick = {
                        }) {
                            Text(text = "同意", color = Color.Gray)
                        }
                    }

                }
            }
        }
    }
}