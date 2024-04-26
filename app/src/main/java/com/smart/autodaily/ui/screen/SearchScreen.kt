package com.smart.autodaily.ui.screen

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.smart.autodaily.R
import com.smart.autodaily.constant.AppBarTitle
import com.smart.autodaily.constant.Ui
import com.smart.autodaily.ui.conponent.RowScriptInfo
import com.smart.autodaily.ui.conponent.SearchTopAppBar
import com.smart.autodaily.ui.conponent.SwipeRefreshList
import com.smart.autodaily.viewmodel.SearchViewModel


@Composable
fun SearchScreen(
    modifier: Modifier,
    searchViewModel: SearchViewModel = viewModel()
){
    var searchText by remember { mutableStateOf("") }
    val netSearScriptList = searchViewModel.searchScriptByPage(searchText).collectAsLazyPagingItems()
    Scaffold (
        topBar = {
            SearchTopAppBar(searchButtonText = AppBarTitle.SEARCH_SCREEN, onSearchClick = {
                searchText = it
            })
        },
    ){
        SwipeRefreshList(
            collectAsLazyPagingItems = netSearScriptList,
            modifier =modifier.padding(it),
            listContent ={scriptInfo ->
                var isDownloaded by remember { mutableIntStateOf(scriptInfo.is_downloaded) }
                RowScriptInfo(
                    cardOnClick = {},
                    scriptInfo = scriptInfo,
                    iconInfo ={
                        IconButton(
                            modifier = Modifier
                                .border(
                                    width = Ui.SPACE_1,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(50)
                                )
                                .size(Ui.ICON_SIZE_40),
                            content ={
                                if (isDownloaded == 1){
                                    Icon(
                                        painter = painterResource(id = R.drawable.baseline_download_done_24),
                                        contentDescription = null
                                    )
                                }else{
                                    Icon(
                                        painter = painterResource(id = R.drawable.baseline_download_24),
                                        contentDescription = null
                                    )
                                }

                            },
                            onClick = {
                                if (scriptInfo.is_downloaded != 1){
                                    searchViewModel.downScriptByScriptId( scriptInfo )
                                    isDownloaded = 1
                                }
                            }
                        )
                    }
                )
            }
        )
    }
}