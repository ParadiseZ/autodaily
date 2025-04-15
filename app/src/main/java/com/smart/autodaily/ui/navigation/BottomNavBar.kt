package com.smart.autodaily.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.smart.autodaily.constant.NavigationItem
import com.smart.autodaily.constant.Screen

/**
 * 底部导航栏组件
 */
@Composable
fun BottomNavBar(navController: NavHostController) {
    // 定义底部导航栏项目
    // 获取当前导航栈状态
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // 只在主要导航目标显示底部导航栏
    NavigationBar {
        NavigationItem.showItem.forEach {item ->
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
                            navController.navSingleTopTo(item.route)
                        } catch (_: Exception) {
                            // 导航失败时的处理
                            navController.navSingleTopTo(Screen.HOME.name)
                        }
                    }
                }
            )
        }
    }
}