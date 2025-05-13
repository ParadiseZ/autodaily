package com.smart.autodaily.ui.screen

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.smart.autodaily.constant.Screen
import com.smart.autodaily.utils.hasNotificationPermission
import splitties.init.appCtx
import kotlin.system.exitProcess


@Composable
fun NotificationRequest(
    modifier: Modifier,
    nav: NavHostController,
){
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted){
                nav.navigate(Screen.HOME.name){
                    launchSingleTop = true
                    popUpTo(0) {
                        inclusive = true // 或 true，看是否需要重建起始页
                    }
                }
            }else{
                exitProcess(0)
            }
        }
    )
    val openSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {
            if (hasNotificationPermission()){
                nav.navigate(Screen.HOME.name){
                    launchSingleTop = true
                    popUpTo(0) {
                        inclusive = true // 或 true，看是否需要重建起始页
                    }
                }
            }else{
                exitProcess(0)
            }
        }
    )
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "重要：关闭电池优化",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "为了保持应用在后台正常运行,请关闭电池优化。小米澎湃参考：设置 → 应用设置，选择AutoDaily → 电量消耗 → 无限制，其他参考：设置→应用程序→AutoDaily→不优化。",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "重要：需要通知权限",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "为了保持应用在后台正常运行，需要通知权限。",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // Android 13及以上版本请求权限
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    // Android 13以下版本跳转到系统设置
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", appCtx.packageName, null)
                    }
                    openSettingsLauncher.launch(intent)
                }
            }
        ) {
            Text(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) "授予权限" else "去设置")
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}