package com.smart.autodaily

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
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.smart.autodaily.constant.NavigationItem
import com.smart.autodaily.ui.conponent.AppNavHost
import com.smart.autodaily.ui.conponent.navSingleTopTo
import com.smart.autodaily.ui.theme.AutoDailyTheme

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

@Composable
fun MainScreen() {
    //Greeting("Android")
    val navController = rememberNavController()
    Scaffold (
        bottomBar = {
            BottomNavigation (
                /*  设置下面的背景色*/
                backgroundColor = MaterialTheme.colorScheme.primaryContainer
            ){
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                NavigationItem.allItems.forEach { screen ->
                    val isSelected =  currentDestination?.hierarchy?.any { it.route == screen.route }
                    BottomNavigationItem(
                        alwaysShowLabel = false,
                        icon = {
                            if ( isSelected == true ){
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
        }
    ){
        AppNavHost( modifier = Modifier.padding(it), navController = navController)
    }
}