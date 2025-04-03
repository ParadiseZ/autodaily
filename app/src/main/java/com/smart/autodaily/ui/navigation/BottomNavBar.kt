package com.smart.autodaily.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.smart.autodaily.constant.NavigationItem
import com.smart.autodaily.constant.Screen

/**
 * 底部导航栏组件
 */
val showBottomBarList by lazy {
    listOf(
        Screen.SEARCH.name,
        Screen.LOG.name,
        Screen.HOME.name,
        Screen.SETTING.name,
        Screen.PERSONAL.name
    )
}
@Composable
fun BottomNavBar(navController: NavController) {
    // 定义底部导航栏项目
    // 获取当前导航栈状态
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // 只在主要导航目标显示底部导航栏
    val showBottomBar = currentDestination?.route in showBottomBarList
    if (showBottomBar) {
        NavigationBar {
            NavigationItem.showItem.forEach { item ->
                // 增加空值检查和错误处理
                val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true

                NavigationBarItem(
                    icon = {
                        if (selected){
                            Icon(imageVector = item.selectedIcon, contentDescription = item.text)
                        }else{
                            Icon(imageVector = item.icon, contentDescription = null)
                        }
                    },
                    label = {
                        if (selected == true){
                            Text(text = item.text, textAlign = TextAlign.Center)
                        }
                    },
                    selected = selected,
                    onClick = {
                        if (currentDestination?.route != item.route) {
                            try {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            } catch (e: Exception) {
                                // 导航失败时的处理
                                navController.navigate(item.route) {
                                    popUpTo(NavigationItem.HOME.route) {
                                        inclusive = true
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}