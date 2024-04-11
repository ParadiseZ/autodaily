package com.smart.autodaily.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.ExperimentalMaterialApi
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.smart.autodaily.constant.Ui
import com.smart.autodaily.data.entity.ScriptInfo
import com.smart.autodaily.viewmodel.SearchViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun SearchScreen(
    modifier: Modifier,
    searchViewModel: SearchViewModel = viewModel()
){
    val netSearScriptList = searchViewModel.getPagingData().collectAsLazyPagingItems()
    if (netSearScriptList.itemSnapshotList.size >0 ){
        HasScriptInfo(modifier = modifier, searchViewModel, netSearScriptList)
    }else{
        EmptyInfo(modifier = modifier)
    }

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun HasScriptInfo(
    modifier: Modifier,
    searchViewModel: SearchViewModel,
    netSearScriptList  : LazyPagingItems<ScriptInfo>
){
    val state = rememberPullRefreshState(refreshing = searchViewModel.refreshing.value, onRefresh = {
        //加载数据
        searchViewModel.viewModelScope.launch {
            searchViewModel.refreshing.value = true
            delay(3000)
            searchViewModel.refreshing.value = false
        }
    })
    Box(modifier = Modifier){
        LazyColumn(
            modifier = Modifier
                .pullRefresh(state),
            contentPadding = PaddingValues(horizontal = Ui.SPACE_10, vertical = Ui.SPACE_5),
            verticalArrangement = Arrangement.spacedBy( Ui.SPACE_5 )
        ){
            this.itemsIndexed(items =  netSearScriptList.itemSnapshotList.items ){ idx, it->
                RowList(
                    scriptInfo = it,
                )
            }
        }
    }
}
@Composable
private fun RowList(
    scriptInfo : ScriptInfo
){
    Text(text = scriptInfo.scriptName)
}

@Composable
fun EmptyInfo(
    modifier: Modifier
){
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        Text(text = "没找到数据")
    }
}