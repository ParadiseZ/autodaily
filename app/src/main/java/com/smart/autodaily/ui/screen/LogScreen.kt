package com.smart.autodaily.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.smart.autodaily.R
import com.smart.autodaily.constant.AppBarTitle
import com.smart.autodaily.constant.Ui
import com.smart.autodaily.utils.SnackbarUtil.CustomSnackbarHost
import com.smart.autodaily.viewmodel.LogViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun LogScreen(
    modifier: Modifier,
    viewModel: LogViewModel = viewModel()
) {
    val logs = viewModel.logStateFlow.collectAsLazyPagingItems()
    val logEnable by viewModel.enableLog.collectAsState()
    val refreshFlag = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val dialog = remember { mutableStateOf(false) }
    val  refreshState =  rememberPullRefreshState(refreshing = refreshFlag.value, onRefresh = {
        //下拉刷新
        scope.launch{
            //collectAsLazyPagingItems.refresh()
            refreshFlag.value = true
            viewModel.loadLogs()
            refreshFlag.value = false
        }
    })
    LaunchedEffect (true){
        viewModel.loadLogs()
    }
    Scaffold (
        modifier = Modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = AppBarTitle.LOG_SCREEN)
                },
                actions = {
                    IconButton(
                        onClick = {
                            dialog.value = !dialog.value
                        }
                    ) {
                        Icon(painter = painterResource(id = R.drawable.outline_delete_24), contentDescription = null)
                    }
                }
            )
        },
        snackbarHost = {
            CustomSnackbarHost()
        }
    ){ padding->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .pullRefresh(refreshState),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
            ) {
                if (logs.itemCount==0){
                    item{
                        if (logEnable){
                            Text(text = "暂无日志，请先运行后查看")
                        }else{
                            Text(text = "若想查看运行情况，请到设置页开启日志")
                        }
                    }
                }
                items(logs.itemCount) { idx ->
                    logs[idx]?.let {
                        Text(text = it)
                    }
                }
            }
            PullRefreshIndicator(refreshFlag.value, refreshState,modifier = Modifier.align(Alignment.TopCenter))
        }
    }

    if(dialog.value){
        AlertDialog(
            onDismissRequest = {
                dialog.value = false
            },
            confirmButton = {
                TextButton(
                    enabled = dialog.value,
                    onClick = {
                        viewModel.deleteLogs()
                        viewModel.loadLogs()
                        dialog.value = false
                    }
                ){
                    Text(text = "确定")
                }
            },
            dismissButton = {
                TextButton(
                    enabled = dialog.value,
                    onClick = {
                        dialog.value = false
                    }
                ){
                    Text(text = "取消")
                }
            },
            title = {
                Text(
                    text = "确认操作",
                    fontWeight = FontWeight.W700,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            text = {
                Text(
                    text = "您确定要清空日志吗？",
                    fontSize = Ui.SIZE_16
                )
            },
        )
    }
}
