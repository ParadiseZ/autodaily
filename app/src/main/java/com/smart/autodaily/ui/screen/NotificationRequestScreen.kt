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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.smart.autodaily.constant.NavigationItem
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
                nav.navigate(NavigationItem.HOME.route){
                    popUpTo(NavigationItem.NOTIFICATION.route) { inclusive = true }
                    launchSingleTop = true
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
                nav.navigate(NavigationItem.HOME.route){
                    popUpTo(NavigationItem.NOTIFICATION.route) { inclusive = true }
                    launchSingleTop = true
                }
            }else{
                exitProcess(0)
            }
        }
    )
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "需要通知权限",
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
    }
}