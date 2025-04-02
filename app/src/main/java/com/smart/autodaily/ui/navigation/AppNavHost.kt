package com.smart.autodaily.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.smart.autodaily.constant.NavigationItem
import com.smart.autodaily.ui.screen.HomeScreen
import com.smart.autodaily.ui.screen.LicenseScreen
import com.smart.autodaily.ui.screen.LicenseShowScreen
import com.smart.autodaily.ui.screen.LogScreen
import com.smart.autodaily.ui.screen.NotificationRequest
import com.smart.autodaily.ui.screen.PersonScreen
import com.smart.autodaily.ui.screen.SearchScreen
import com.smart.autodaily.ui.screen.SettingScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    startDestination : String = NavigationItem.HOME.route,
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination,
        //startDestination = NavigationItem.PERSON.route,
    ) {

        composable(NavigationItem.SEARCH.route) {
            SearchScreen(modifier)
        }
        composable(NavigationItem.LOG.route) {
            LogScreen(modifier)
        }
        composable(NavigationItem.HOME.route) {
            HomeScreen(modifier, navController)
        }
        composable(NavigationItem.SETTING.route) {
            SettingScreen(modifier, navController)
        }
        composable(NavigationItem.PERSON.route){
            PersonScreen(modifier, navController)
        }


        composable(NavigationItem.LICENSE.route){
            LicenseScreen(navController)
        }
        composable(
            route = NavigationItem.LICENSESHOW.route,
            arguments = listOf(navArgument("data"){type = NavType.StringType})
        ){
            backStackEntry ->
            LicenseShowScreen(modifier, navController,
                backStackEntry.arguments?.getString("data").toString()
            )
        }
        composable(NavigationItem.NOTIFICATION.route){
            NotificationRequest(modifier,navController)
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