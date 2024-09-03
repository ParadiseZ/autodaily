package com.smart.autodaily

import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.smart.autodaily.ui.theme.AutoDailyTheme

class LicenseShowActivity  : ComponentActivity(){
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val name = intent.getStringExtra("showLicense")
        var title = ""
        name?.let {

            var fileName = ""
            if(name == "PRIVACY"){
                fileName = "privacy.html"
                title = "隐私政策"
            }else if(name == "TERMS_OF_USE"){
                fileName = "terms.html"
                title = "使用条款"
            }
            setContent {
                AutoDailyTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ){
                        Scaffold (
                            topBar = {
                                TopAppBar(
                                    title = {
                                        Text(text = title)
                                    },
                                    navigationIcon = {
                                        IconButton(onClick ={finish()}
                                        ){
                                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                                        }
                                    }
                                )
                            },
                            content ={
                                WebViewContainer("file:///android_asset/$fileName",it)
                            }
                        )
                    }
                }
            }
        }  ?: {
            finish()
        }
    }
}


@Composable
fun WebViewContainer(
    url: String,
    paddingValues: PaddingValues
) {
    AndroidView(
        modifier = Modifier.padding(paddingValues),
        factory = { context ->
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            webViewClient = WebViewClient()
            loadUrl(url)
        }
    })
}
