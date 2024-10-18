package com.smart.autodaily.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Modifier
import com.smart.autodaily.constant.AppBarTitle
import com.smart.autodaily.ui.screen.ScriptSetScreen
import com.smart.autodaily.ui.theme.AutoDailyTheme

class ScriptSetActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val curId = intent.getIntExtra("CUR_SCRIPT_ID",0)
        setContent {
            // Theme wrappers and other composables may be needed based on your application setup
            AutoDailyTheme {
                Scaffold (
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(text = AppBarTitle.SCRIPT_SET_DETAIL)
                            },
                            navigationIcon = {
                                IconButton(onClick ={finish()}
                                ){
                                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                                }
                            }
                        )
                    }
                ){
                    ScriptSetScreen(
                        modifier = Modifier.padding(it),
                        selectId = curId
                    )
                }
            }

        }
    }
}