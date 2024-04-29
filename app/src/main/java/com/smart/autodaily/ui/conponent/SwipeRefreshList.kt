package com.smart.autodaily.ui.conponent

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ButtonDefaults.textButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.smart.autodaily.R
import com.smart.autodaily.constant.Ui
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 下拉加载封装
 *
 * */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <T : Any> SwipeRefreshList(
    collectAsLazyPagingItems: LazyPagingItems<T>,
    listContent: @Composable (T) -> Unit,
    modifier: Modifier
) {
    val scope = rememberCoroutineScope()
    var refreshFlag by remember { mutableStateOf(false) }
    val isLoading = with(collectAsLazyPagingItems.loadState) {
        refresh is LoadState.Loading || refresh is LoadState.Error
    }//避免是Error并且不是Loading的时候，refreshFlag=false隐藏。需加载结束时，再次赋值隐藏刷新图标，避免加载异常的提示相关内容不会显示
    val  refreshState =  rememberPullRefreshState(refreshing = refreshFlag, onRefresh = {
        //下拉刷新
        scope.launch{
            collectAsLazyPagingItems.refresh()
            refreshFlag = true
            delay(Ui.DELAY_TIME)
            refreshFlag = isLoading
        }
    })
    Box(
        modifier = modifier.pullRefresh(refreshState),
    ){
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            contentPadding = PaddingValues(horizontal = Ui.SPACE_10, vertical = Ui.SPACE_10),
            verticalArrangement = Arrangement.spacedBy( Ui.SPACE_10 )
        ) {
            items( collectAsLazyPagingItems.itemCount ){index ->
                collectAsLazyPagingItems[index]?.let { item ->
                    listContent(item)
                }
            }
            collectAsLazyPagingItems.apply {
                when(this.loadState.refresh) {
                    is LoadState.Loading -> {
                        refreshFlag = true
                    }
                    is LoadState.Error -> {
                        refreshFlag = false //加载超过1.5秒，加载异常时再次赋予加载图标状态
                        if (collectAsLazyPagingItems.itemCount <= 0) {
                            //刷新的时候，如果itemCount小于0，第一次加载异常
                            item {
                                ErrorContent {
                                    collectAsLazyPagingItems.retry()
                                }
                            }
                        } else {
                            item {
                                ErrorMoreRetryItem {
                                    collectAsLazyPagingItems.retry()
                                }
                            }
                        }
                    }
                    is LoadState.NotLoading -> {//加载超过1.5秒，停止加载时再次赋予加载图标状态，!isLoading避免加载失败时也隐藏
                        if(!isLoading) {
                            scope.launch {
                                delay(Ui.DELAY_TIME)
                                refreshFlag = false
                            }
                        }
                    }
                }
                when(this.loadState.append) {
                    is LoadState.Loading -> {
                        item {
                            LoadingContent()
                        }
                    }
                    is LoadState.Error -> {
                        //加载更多异常
                        item {
                            ErrorMoreRetryItem{
                                collectAsLazyPagingItems.retry()
                            }
                        }
                    }

                    is LoadState.NotLoading  -> {
                        if(!isLoading) {
                            scope.launch {
                                delay(Ui.DELAY_TIME)
                                refreshFlag = false
                            }
                        }
                    }
                }
            }
        }
        PullRefreshIndicator(refreshFlag, refreshState,modifier = Modifier.align(Alignment.TopCenter))
    }
}

/**
 * 底部加载更多失败处理
 * */
@Composable
fun ErrorMoreRetryItem(retry: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        TextButton(
            onClick = { retry() },
            modifier = Modifier
                .padding(20.dp)
                .width(80.dp)
                .height(30.dp),
            shape = RoundedCornerShape(6.dp),
            contentPadding = PaddingValues(3.dp),
            colors = textButtonColors()
        ) {
            Text(text = "请重试")
        }
    }
}

/**
 * 页面加载失败处理
 * */
@Composable
fun ErrorContent(retry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 100.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier.padding(top = Ui.SPACE_80),
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = null
        )
        Text(text = "请求失败，请检查网络或稍后重试", modifier = Modifier.padding(top = 8.dp, bottom = 6.dp))
        TextButton(
            onClick = { retry() },
            modifier = Modifier
                .width(80.dp)
                .height(34.dp),
            shape = RoundedCornerShape(10.dp),
            colors =textButtonColors(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(0.2f))
        ) { Text(text = "重试") }
    }
}

/**
 * 页面加载中处理
 * */
@Composable
fun LoadingContent() {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "正在加载数据。。。", modifier = Modifier.padding(top = 8.dp, bottom = 6.dp))
    }
}