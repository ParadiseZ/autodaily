package com.smart.autodaily.ui.conponent

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.smart.autodaily.App
import com.smart.autodaily.viewmodel.ApplicationViewModel

@Composable
fun appViewModel() :ApplicationViewModel{
    val context = LocalContext.current.applicationContext as App
    val viewModel: ApplicationViewModel = remember {
        context.viewModel
    }
    return viewModel
}