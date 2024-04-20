package com.smart.autodaily.ui.conponent

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.smart.autodaily.constant.NavigationItem
import com.smart.autodaily.constant.Ui
import com.smart.autodaily.ui.screen.HomeScreen
import com.smart.autodaily.ui.screen.LogScreen
import com.smart.autodaily.ui.screen.PersonalScreen
import com.smart.autodaily.ui.screen.SearchScreen
import com.smart.autodaily.ui.screen.SettingScreen
import com.smart.autodaily.viewmodel.HomeViewMode
import com.smart.autodaily.viewmodel.SearchViewModel

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    NavHost(
        modifier = Modifier.padding( Ui.SPACE_5 ),
        navController = navController,
        startDestination = NavigationItem.HOME.route,
    ) {

        composable(NavigationItem.Search.route) {
            SearchScreen(modifier)
        }
        composable(NavigationItem.Log.route) {
            LogScreen(modifier)
        }
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