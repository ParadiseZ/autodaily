package com.smart.autodaily.ui.conponent

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp

@Composable
fun MyButton(
    modifier: Modifier = Modifier,
    text : String,
    onclick : () -> Unit = {}
) {
    Button(
        modifier = modifier
            .width(84.dp)
            .height(44.dp)
            .padding(5.dp),
        onClick = {
            onclick()
        }) {
        Text(text = text, fontSize = TextUnit(12f, TextUnitType.Sp))
    }
}