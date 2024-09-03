package com.smart.autodaily.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.smart.autodaily.R
import com.smart.autodaily.constant.AppBarTitle
import com.smart.autodaily.constant.Ui
import com.smart.autodaily.ui.conponent.RowScriptInfo
import com.smart.autodaily.ui.conponent.SearchTopAppBar
import com.smart.autodaily.ui.conponent.SwipeRefreshList
import com.smart.autodaily.viewmodel.SearchViewModel
import kotlin.random.Random


@Composable
fun SearchScreen(
    modifier: Modifier,
    searchViewModel: SearchViewModel = viewModel(),
){
    val loadDataFlagFlow by searchViewModel.loadDataFlagFlow.collectAsState()
    val netSearScriptList = searchViewModel.remoteScriptList.collectAsLazyPagingItems()
    val color = arrayListOf(0xff95e1d3,0xff71c9ce,0xffa6e3e9,0xffcbf1f5,0xffe3fdfd,0xffeaffd0,0xfffce38a,0xfffce38a,0xfff38181,0xfff3f798,0xffeab4f8,0xfffcc8f8,0xffc7f5fe)
    LaunchedEffect(key1 = loadDataFlagFlow) {
        searchViewModel.getRemoteScriptList(searchViewModel.appViewModel.localScriptListAll.value)
    }
    Scaffold (
        topBar = {
            SearchTopAppBar(searchButtonText = AppBarTitle.SEARCH_SCREEN, onSearchClick = {
                searchViewModel.changeSearchText(it)
            })
        },
    ){
        SwipeRefreshList(
            collectAsLazyPagingItems = netSearScriptList,
            modifier =modifier.padding(it),
            listContent ={scriptInfo ->
                var isDownloaded by remember { mutableIntStateOf(scriptInfo.isDownloaded) }
                RowScriptInfo(
                    cardOnClick = {},
                    scriptInfo = scriptInfo,
                    surface = {
                      Box (
                          modifier = Modifier
                              .size(50.dp)
                              .clip(RoundedCornerShape(50))
                              .background(Color(color[Random.nextInt(0, color.size)])),
                          contentAlignment = Alignment.Center
                      ) {
                          Text(text = scriptInfo.scriptName.substring(0,1), color = Color.White)
                      }
                    },
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
                                if (scriptInfo.isDownloaded != 1){
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