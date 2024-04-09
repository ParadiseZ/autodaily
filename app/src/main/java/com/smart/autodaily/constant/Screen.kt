package com.smart.autodaily.constant

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class Screen {
    Search,
    Log,
    HOME,
    SETTING,
    PERSONAL
}

sealed class NavigationItem(val route: String, val icon: ImageVector,val selectedIcon: ImageVector, val text: String, val selected: Boolean = false) {
    object Search : NavigationItem(Screen.Search.name, Icons.Outlined.Search, Icons.Filled.Search, "搜索")
    object Log : NavigationItem(Screen.Log.name, Icons.AutoMirrored.Outlined.List, Icons.AutoMirrored.Filled.List, "日志")
    object HOME : NavigationItem(Screen.HOME.name, Icons.Outlined.Home, Icons.Filled.Home, "列表")
    object SETTING : NavigationItem(Screen.SETTING.name, Icons.Outlined.Settings, Icons.Filled.Settings, "设置")
    object PERSONAL : NavigationItem(Screen.PERSONAL.name, Icons.Outlined.Person, Icons.Filled.Person, "我的")

    companion object {
        val allItems = listOf(Search, Log, HOME, SETTING, PERSONAL)
    }
}