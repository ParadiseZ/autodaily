package com.smart.autodaily.ui.conponent

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.smart.autodaily.constant.BorderDirection

@Composable
fun TitleSettingItem(
    title: String,
    modifier: Modifier = Modifier
){
    SingleBorderBox(direction = BorderDirection.BOTTOM) {
        Row (
            modifier = modifier.fillMaxWidth()
        ){
            Text(text = "★$title")
        }
    }
}