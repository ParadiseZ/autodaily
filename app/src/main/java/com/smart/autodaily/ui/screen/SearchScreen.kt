package com.smart.autodaily.ui.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.smart.autodaily.R
import com.smart.autodaily.constant.ScreenText
import com.smart.autodaily.constant.Ui
import com.smart.autodaily.data.entity.ScriptInfo
import com.smart.autodaily.ui.conponent.IconButtonCustom
import com.smart.autodaily.ui.conponent.RowListCustom
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
            SearchTopAppBar(searchButtonText = ScreenText.SEARCH_SCREEN, onSearchClick = {
                searchText = it
            })
        },
    ){
        SwipeRefreshList(
            collectAsLazyPagingItems = netSearScriptList,
            modifier =modifier.padding(it),
            listContent ={scriptInfo ->
                var isDownloaded by remember { mutableIntStateOf(scriptInfo.is_downloaded) }
                RowListCustom(
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