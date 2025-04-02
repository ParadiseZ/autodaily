package com.smart.autodaily.ui.screen

import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicenseShowScreen(
    modifier: Modifier,
    navController: NavHostController,
    target: String
){
    var title = ""
    var fileName = ""
    when(target){
        "PRIVACY" -> {
            title = "隐私政策"
            fileName = "privacy.html"
        }
        "TERMS_OF_USE" -> {
            title = "使用条款"
            fileName = "terms.html"
        }
    }
    Scaffold (
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = title)
                },
                navigationIcon = {
                    IconButton(onClick ={
                        navController.popBackStack()
                    }
                    ){
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        content ={
            AndroidView(
                modifier = modifier.padding(it),
                factory = { context ->
                    WebView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        webViewClient = WebViewClient()
                        loadUrl("file:///android_asset/$fileName")
                    }
                }
            )
        }
    )
}