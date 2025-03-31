package com.smart.autodaily.utils

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch

object SnackbarUtil {
    private var snackbarHostState: SnackbarHostState? = null
    private var scope: CoroutineScope = MainScope()

    @Composable
    fun CustomSnackbarHost() {
        snackbarHostState = remember { SnackbarHostState() }
        scope = rememberCoroutineScope()
        SnackbarHost(snackbarHostState!!)
    }

    fun show(message: String) {
        scope.coroutineContext.cancelChildren()
        scope.launch {
            snackbarHostState?.showSnackbar(message)
        }
    }
}