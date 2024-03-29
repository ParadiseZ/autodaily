package com.smart.autodaily.ui.conponent

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.smart.autodaily.constant.NavigationItem
import com.smart.autodaily.ui.Screen.HomeScreen
import com.smart.autodaily.ui.Screen.PersonalScreen
import com.smart.autodaily.ui.Screen.SettingScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = NavigationItem.SETTING.route,
    ) {
        composable(NavigationItem.HOME.route) {
            HomeScreen(modifier, navController)
        }
        composable(NavigationItem.SETTING.route) {
            SettingScreen(modifier, navController)
        }
        composable(NavigationItem.PERSONAL.route){
            PersonalScreen(modifier, navController)
        }
    }
}

fun NavHostController.navSingleTopTo(route: String) =
    this.navigate(route) {
        popUpTo(
            this@navSingleTopTo.graph.findStartDestination().id
        ) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }

//private fun NavHostController.navToSingle(accountType: String) {
//    this.navSingleTopTo("${ScriptInfo.route}/$accountType")
//}