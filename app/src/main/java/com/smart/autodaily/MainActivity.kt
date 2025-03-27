package com.smart.autodaily

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.edit
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.smart.autodaily.constant.NavigationItem
import com.smart.autodaily.ui.conponent.AppNavHost
import com.smart.autodaily.ui.conponent.floatingView
import com.smart.autodaily.ui.conponent.initAlertWindow
import com.smart.autodaily.ui.conponent.navSingleTopTo
import com.smart.autodaily.ui.screen.UpdateScreen
import com.smart.autodaily.ui.theme.AutoDailyTheme
import com.smart.autodaily.utils.ShizukuUtil
import com.smart.autodaily.utils.binderScope
import com.smart.autodaily.utils.cancelJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import splitties.alertdialog.appcompat.alertDialog
import splitties.systemservices.notificationManager

class MainActivity : ComponentActivity(), CoroutineScope by MainScope() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // 无论结果如何，都记录已经检查过权限
        App.sharedPreferences.edit { putBoolean(App.KEY_NOTIFICATION_PERMISSION_CHECKED, true) }
    }

    override fun onStart() {
        super.onStart()
        ShizukuUtil.initShizuku()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 检查通知权限
        checkNotificationPermission()
        
        setContent {
            AutoDailyTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination?.hierarchy
                    Scaffold (
                        bottomBar = {
                            NavigationBar{
                                NavigationItem.allItems.forEach{ screen ->
                                    val isSelected =  currentDestination?.any { it.route == screen.route }
                                    NavigationBarItem(
                                        icon = {
                                            if (isSelected == true){
                                                Icon(imageVector = screen.selectedIcon, contentDescription = screen.text)
                                            }else{
                                                Icon(imageVector = screen.icon, contentDescription = null)
                                            }
                                        },
                                        label = {
                                            if (isSelected == true){
                                                Text(text = screen.text, textAlign = TextAlign.Center)
                                            }
                                        },
                                        selected = isSelected == true,
                                        onClick = {
                                            navController.navSingleTopTo(screen.route)
                                        }
                                    )
                                }

                            }
                        },
                        content = {
                            UpdateScreen()
                            AppNavHost( modifier = Modifier.padding(it), navController = navController)
                        }
                    )
                }
            }
        }


        /*launch(Dispatchers.Main) {
            if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.R){
                val startActivityForResultLauncher  =registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    if (result.resultCode == RESULT_OK) {
                        val intent = Intent(this@MainActivity, MediaProjectionService::class.java)
                        intent.putExtra("code",result.resultCode)
                        intent.putExtra("data", result.data)
                        ScreenCaptureUtil.displayMetrics = ScreenCaptureUtil.getDisplayMetrics(this@MainActivity)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(intent)
                        }else{
                            startService(intent)
                        }
                    }else {
                        this@MainActivity.toastOnUi("拒绝了录屏申请，将无法运行")
                    }
                    // stopService(intent)
                }
                val mediaProjectionManager = baseContext.getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                ScreenCaptureUtil.mediaProjectionDataMap["mediaProjectionManager"] = mediaProjectionManager
                ScreenCaptureUtil.mediaProjectionDataMap["mediaProjectionIntent"] = mediaProjectionManager.createScreenCaptureIntent()
                ScreenCaptureUtil.mediaProjectionDataMap["startActivityForResultLauncher"] = startActivityForResultLauncher
            }

        }*/
    }

    private fun checkNotificationPermission() {
        // 检查是否已经检查过权限
        if (App.sharedPreferences.getBoolean(App.KEY_NOTIFICATION_PERMISSION_CHECKED, true)) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!notificationManager.areNotificationsEnabled()) {
                showNotificationPermissionDialog()
            }
        }
    }

    private fun showNotificationPermissionDialog() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launch {
                try {
                    alertDialog(
                        title = "需要通知权限",
                        message = "为了确保您能及时收到重要通知，请允许应用发送通知。",
                        isCancellable = true
                    ) {
                        setPositiveButton("去设置") { _, _ ->
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = android.net.Uri.fromParts("package", packageName, null)
                            }
                            notificationPermissionLauncher.launch(intent)
                        }
                        setNegativeButton("暂不设置") { _, _ ->
                            // 用户点击"暂不设置"时也记录状态
                            App.sharedPreferences.edit { putBoolean(App.KEY_NOTIFICATION_PERMISSION_CHECKED, true) }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            ShizukuUtil.removeShizuku()
        }
        if (floatingView != null){
            windowManager.removeView(floatingView)
        }
        //所有协程
        cancelJob()
        //binder协程
        binderScope.cancel()
        //main
        cancel()
    }

    fun requestOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            activityResultRegistry.register(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode != RESULT_OK) {
                    initAlertWindow(700,400)
                }
            }.run {
                launch(intent)
            }
        } else {
            initAlertWindow(700,400)
        }
    }
}