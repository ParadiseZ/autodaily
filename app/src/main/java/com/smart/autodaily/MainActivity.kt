package com.smart.autodaily

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.jeremyliao.liveeventbus.LiveEventBus
import com.smart.autodaily.constant.Screen
import com.smart.autodaily.ui.conponent.floatingView
import com.smart.autodaily.ui.navigation.AppNavHost
import com.smart.autodaily.ui.theme.AutoDailyTheme
import com.smart.autodaily.utils.BitmapPool
import com.smart.autodaily.utils.RootUtil
import com.smart.autodaily.utils.ShizukuUtil
import com.smart.autodaily.utils.SnackbarUtil
import com.smart.autodaily.utils.binderScope
import com.smart.autodaily.utils.cancelJob
import com.smart.autodaily.utils.hasNotificationPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

class MainActivity : ComponentActivity(), CoroutineScope by MainScope() {

    override fun onStart() {
        super.onStart()
        ShizukuUtil.initShizuku()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            AutoDailyTheme {
                val target = if(!hasNotificationPermission()){
                    Screen.NOTIFICATION.name
                }else{
                    Screen.HOME.name
                }

                // A surface container using the 'background' color from the theme
                val navHostController = rememberNavController()
                LiveEventBus
                    .get("loginCheck", String::class.java)
                    .observe(this) {
                        navHostController.navigate(Screen.LOGIN.name){
                            launchSingleTop = true
                            popUpTo(0) {
                                inclusive = true // 或 true，看是否需要重建起始页
                            }
                        }
                    }
                MainScreen(navController = navHostController, startDestination = target)
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
        BitmapPool.clear()
        RootUtil.close()
    }
}

@Composable
fun MainScreen(
    navController: NavHostController,
    startDestination: String
){
    Scaffold (
        snackbarHost = {
            SnackbarUtil.CustomSnackbarHost()
        }
    ){
        AppNavHost( modifier = Modifier.padding(it), navController = navController ,startDestination = startDestination)
    }
}