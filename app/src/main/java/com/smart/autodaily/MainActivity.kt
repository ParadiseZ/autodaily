package com.smart.autodaily

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.BottomNavigation
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedButton
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.smart.autodaily.constant.NavigationItem
import com.smart.autodaily.constant.Ui
import com.smart.autodaily.ui.conponent.AppNavHost
import com.smart.autodaily.ui.conponent.navSingleTopTo
import com.smart.autodaily.ui.theme.AutoDailyTheme
import com.smart.autodaily.utils.ScreenUtil
import com.smart.autodaily.viewmodel.HomeViewMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AutoDailyTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@Preview
@Composable
fun MainScreen() {
    //Greeting("Android")
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val homeViewModel :  HomeViewMode=  viewModel()
    val context : Context = LocalContext.current

    Scaffold (
        bottomBar = {
            BottomNavigation (
                /*  设置下面的背景色*/
                backgroundColor = MaterialTheme.colorScheme.primaryContainer
            ){
                NavigationItem.allItems.forEach { screen ->
                    val isSelected =  currentDestination?.hierarchy?.any { it.route == screen.route }
                    BottomNavigationItem(
                        alwaysShowLabel = false,
                        icon = {
                            if (isSelected == true){
                                Icon(imageVector = screen.selectedIcon, contentDescription = screen.text)
                            }else{
                                Icon(imageVector = screen.icon, contentDescription = null)
                            }
                        },
                        label = {
                            if(isSelected == true){
                                Text(text = screen.text, textAlign = TextAlign.Center)
                            }
                        },
                        //selectedContentColor = MaterialTheme.colorScheme.primary,
                        //unselectedContentColor = MaterialTheme.colorScheme.secondary,
                        //selected = screen.route == Screen.HOME.name,
                        selected = isSelected == true,
                        onClick = {
                            navController.navSingleTopTo(screen.route)
                        }
                    )
                }
            }
        },
        //脚本页面运行按钮
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            if (
                currentDestination?.hierarchy?.any{
                    it.route == NavigationItem.HOME.route
                } == true
            ){
                FloatingActionButton(
                    onClick = {
                        homeViewModel.runButtonClick(context)
                    }
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "开始运行")
                }
            }
        },
        content = {
            AppNavHost( modifier = Modifier.padding(it), navController = navController, homeViewModel)
        }
    )
}