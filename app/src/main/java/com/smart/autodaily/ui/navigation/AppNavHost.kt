package com.smart.autodaily.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.smart.autodaily.constant.Screen
import com.smart.autodaily.ui.screen.HomeScreen
import com.smart.autodaily.ui.screen.LicenseShowScreen
import com.smart.autodaily.ui.screen.LogScreen
import com.smart.autodaily.ui.screen.LoginScreen
import com.smart.autodaily.ui.screen.NotificationRequest
import com.smart.autodaily.ui.screen.PersonScreen
import com.smart.autodaily.ui.screen.RegisterScreen
import com.smart.autodaily.ui.screen.ResetPasswordScreen
import com.smart.autodaily.ui.screen.ScriptSetScreen
import com.smart.autodaily.ui.screen.SearchScreen
import com.smart.autodaily.ui.screen.SettingScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    startDestination : String,
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination,
        //startDestination = NavigationItem.PERSON.route,
    ) {

        composable(Screen.SEARCH.name) {
            SearchScreen(modifier,navController)
        }
        composable(Screen.LOG.name) {
            LogScreen(modifier,navController)
        }
        composable(Screen.HOME.name) {
            HomeScreen(modifier, navController)
        }
        composable(Screen.SETTING.name) {
            SettingScreen(modifier, navController)
        }
        composable(Screen.PERSONAL.name){
            PersonScreen(modifier, navController)
        }

        composable (Screen.LOGIN.name ){
            LoginScreen(modifier, navController)
        }
        composable (Screen.REGISTER.name ){
            RegisterScreen(modifier, navController)
        }
        composable (Screen.RESETPWD.name ){
            ResetPasswordScreen(modifier, navController)
        }


        composable(
            route = Screen.LICENSESHOW.name+"/{data}",
            arguments = listOf(navArgument("data"){type = NavType.StringType})
        ){
            backStackEntry ->
            LicenseShowScreen(modifier, navController,
                backStackEntry.arguments?.getString("data").toString()
            )
        }
        composable(Screen.NOTIFICATION.name){
            NotificationRequest(modifier,navController)
        }
        composable(
            route= Screen.SCRIPTSETDETAIL.name+"/{scriptId}",
            arguments = listOf(navArgument("scriptId"){type = NavType.IntType})
            ){ bse->
            ScriptSetScreen(modifier, nhc=navController,selectId = bse.arguments?.getInt("scriptId"))
        }
    }
}

fun NavController.navSingleTopTo(route: String) {
    this.navigate(route) {
        popUpTo(
            this@navSingleTopTo.graph.findStartDestination().id
        ) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}