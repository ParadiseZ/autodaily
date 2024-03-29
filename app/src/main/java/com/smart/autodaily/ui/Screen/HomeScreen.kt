package com.smart.autodaily.ui.Screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController

@Composable
fun HomeScreen(modifier: Modifier, nhc: NavHostController) {
    Text(text = "Hello Home！")
}