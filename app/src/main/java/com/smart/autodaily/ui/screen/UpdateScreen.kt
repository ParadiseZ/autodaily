package com.smart.autodaily.ui.screen

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smart.autodaily.viewmodel.UpdateState
import com.smart.autodaily.viewmodel.UpdateViewModel
import splitties.init.appCtx

@Composable
fun UpdateScreen(
    vm : UpdateViewModel = viewModel()
){
    val updateState by vm.updateState.collectAsState()
    // 在组件首次加载时检查更新
    LaunchedEffect(Unit) {
        vm.checkUpdate()
    }

    when(updateState){
        is UpdateState.HasUpdate -> {
            val updateInfo = (updateState as UpdateState.HasUpdate).updateInfo
            val mustUpdate = updateInfo.first {
                it.confDesc == "UPAPP_MUST"
            }.confValue == "1"
            AlertDialog(
                onDismissRequest = {
                    if(!mustUpdate){
                        vm.closeDialog()
                    }
                },
                title = {
                    Text(
                        text = "发现新版本 ${updateInfo.first { it.confDesc=="APP_VERSION_NAME" }.confValue}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // 更新内容标题
                        Text(
                            text = "更新内容：",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // 更新内容
                        Text(
                            text = updateInfo.first { it.confDesc== "UPDATE_CONTENT" }.confValue,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, updateInfo.first {
                                it.confDesc == "DOWN_LINK"
                            }.confValue.split(";")[0].toUri())
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            appCtx.startActivity(intent)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text("立即更新")
                    }
                },
                dismissButton = {
                    if (!mustUpdate) {
                        TextButton(
                            onClick = {
                                vm.closeDialog()
                            },
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Text("稍后再说")
                        }
                    }
                }
            )
        }else -> {
        }
    }

}