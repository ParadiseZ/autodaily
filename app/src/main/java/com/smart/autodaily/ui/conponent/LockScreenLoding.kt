package com.smart.autodaily.ui.conponent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun LockScreenLoading(
    isLocked : MutableState<Boolean>,
    content: @Composable () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        content()
        if (isLocked.value) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            Box(modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.3f))) {
                // This Box acts as an overlay to prevent user interaction
            }
        }
    }
}