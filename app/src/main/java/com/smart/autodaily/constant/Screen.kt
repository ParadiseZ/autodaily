package com.smart.autodaily.constant

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector


enum class Screen {
    SEARCH,
    LOG,
    HOME,
    SETTING,
    PERSONAL,

    LICENSESHOW,
    NOTIFICATION,

    LOGIN,
    REGISTER,
    RESETPWD
}

data class  BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val selectedIcon : ImageVector,
    val text: String,
)

object ScreenText {
    const val SEARCH_SCREEN ="搜索"
    const val LOG_SCREEN ="日志"
    const val HOME_SCREEN ="列表"
    const val SETTING_SCREEN ="设置"
    const val PERSON_SCREEN ="我的"
}

object NavigationItem{
    val showItem = listOf(
        BottomNavItem(Screen.SEARCH.name, Icons.Outlined.Search, Icons.Filled.Search, ScreenText.SEARCH_SCREEN),
        BottomNavItem(Screen.LOG.name, Icons.AutoMirrored.Outlined.List, Icons.AutoMirrored.Filled.List, ScreenText.LOG_SCREEN),
        BottomNavItem(Screen.HOME.name, Icons.Outlined.Home, Icons.Filled.Home, ScreenText.HOME_SCREEN),
        BottomNavItem(Screen.SETTING.name, Icons.Outlined.Settings, Icons.Filled.Settings, ScreenText.SETTING_SCREEN),
        BottomNavItem(Screen.PERSONAL.name, Icons.Outlined.Person, Icons.Filled.Person, ScreenText.PERSON_SCREEN),
    )
}