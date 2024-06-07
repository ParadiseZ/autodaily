package com.smart.autodaily

import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
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
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.smart.autodaily.constant.NavigationItem
import com.smart.autodaily.service.MediaProjectionService
import com.smart.autodaily.ui.conponent.AppNavHost
import com.smart.autodaily.ui.conponent.navSingleTopTo
import com.smart.autodaily.ui.theme.AutoDailyTheme
import com.smart.autodaily.utils.ScreenCaptureUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

class MainActivity : ComponentActivity(), CoroutineScope by MainScope() {
    private val startActivityForResultLauncher  =registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val intent = Intent(this, MediaProjectionService::class.java)
            intent.putExtra("code",result.resultCode)
            intent.putExtra("data", result.data)
            ScreenCaptureUtil.displayMetrics = ScreenCaptureUtil.getDisplayMetrics(this)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            }else{
                startService(intent)
            }
        }else {
            println("拒绝了mediaProjection权限申请")
        }
        stopService(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mediaProjectionManager = baseContext.getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        ScreenCaptureUtil.mediaProjectionDataMap["mediaProjectionManager"] = mediaProjectionManager
        ScreenCaptureUtil.mediaProjectionDataMap["mediaProjectionIntent"] = mediaProjectionManager.createScreenCaptureIntent()
        ScreenCaptureUtil.mediaProjectionDataMap["startActivityForResultLauncher"] = startActivityForResultLauncher
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
                            AppNavHost( modifier = Modifier.padding(it), navController = navController)
                        }
                    )
                }
            }
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }
}