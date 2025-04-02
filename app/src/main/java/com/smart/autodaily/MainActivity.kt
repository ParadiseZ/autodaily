package com.smart.autodaily

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.smart.autodaily.constant.NavigationItem
import com.smart.autodaily.ui.conponent.floatingView
import com.smart.autodaily.ui.navigation.AppNavHost
import com.smart.autodaily.ui.navigation.BottomNavBar
import com.smart.autodaily.ui.theme.AutoDailyTheme
import com.smart.autodaily.utils.ShizukuUtil
import com.smart.autodaily.utils.SnackbarUtil
import com.smart.autodaily.utils.binderScope
import com.smart.autodaily.utils.cancelJob
import com.smart.autodaily.utils.hasNotificationPermission
import com.smart.autodaily.viewmodel.LicenseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

class MainActivity : ComponentActivity(), CoroutineScope by MainScope() {

    override fun onStart() {
        super.onStart()
        ShizukuUtil.initShizuku()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val licenseViewModel = LicenseViewModel(application)
        super.onCreate(savedInstanceState)
        // 检查通知权限
        setContent {
            var target = NavigationItem.HOME.route;
            LaunchedEffect(Unit){
                licenseViewModel.getPrivacyRes()
                if (!licenseViewModel.hasAccept.value){
                    target = NavigationItem.LICENSE.route
                }else if(!hasNotificationPermission()){
                    target = NavigationItem.NOTIFICATION.route
                }
            }
            AutoDailyTheme {
                // A surface container using the 'background' color from the theme
                val navController = rememberNavController()
                MainScreen(navController = navController, startDestination = target)
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
}

@Composable
fun MainScreen(
    navController: NavHostController,
    startDestination: String = NavigationItem.HOME.route
){
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold (
            bottomBar = {
                BottomNavBar(navController = navController)
            },
            snackbarHost = {
                SnackbarUtil.CustomSnackbarHost()
            }
        ){
            AppNavHost( modifier = Modifier.padding(it), navController = navController ,startDestination = startDestination)
        }
    }
}