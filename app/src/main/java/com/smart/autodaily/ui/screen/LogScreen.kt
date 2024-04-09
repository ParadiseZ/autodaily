package com.smart.autodaily.ui.screen

import androidx.compose.foundation.layout.Box
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun LogScreen(
    modifier: Modifier
){
    Box(modifier = modifier){
        Text(text = "Hello LogScreen!")
    }
}