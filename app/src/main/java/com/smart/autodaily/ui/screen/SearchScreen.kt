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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import com.smart.autodaily.R
import com.smart.autodaily.constant.AppBarTitle
import com.smart.autodaily.constant.Ui
import com.smart.autodaily.ui.conponent.RowScriptInfo
import com.smart.autodaily.ui.conponent.SearchTopAppBar
import com.smart.autodaily.ui.conponent.SwipeRefreshList
import com.smart.autodaily.ui.navigation.BottomNavBar
import com.smart.autodaily.utils.SnackbarUtil
import com.smart.autodaily.viewmodel.SearchViewModel
import kotlinx.coroutines.launch
import kotlin.random.Random


@Composable
fun SearchScreen(
    modifier: Modifier,
    nhc : NavHostController,
    searchViewModel: SearchViewModel = viewModel(),
){
    val netSearScriptList = searchViewModel.remoteScriptList.collectAsLazyPagingItems()
    val localScriptList by searchViewModel.appViewModel.localScriptListAll.collectAsState()
    val color = arrayListOf(0xff95e1d3,0xff71c9ce,0xffa6e3e9,0xffcbf1f5,0xffe3fdfd,0xffeaffd0,0xfffce38a,0xfffce38a,0xfff38181,0xfff3f798,0xffeab4f8,0xfffcc8f8,0xffc7f5fe)
    LaunchedEffect(key1 = true) {
        searchViewModel.getRemoteScriptList(localScriptList)
    }
    Scaffold (
        bottomBar = {
            BottomNavBar(navController = nhc)
        },
        topBar = {
            SearchTopAppBar(searchButtonText = AppBarTitle.SEARCH_SCREEN, onSearchClick = {
                searchViewModel.changeSearchText(it)
            })
        },
        snackbarHost = {
            SnackbarUtil.CustomSnackbarHost()
        }
    ){
        SwipeRefreshList(
            collectAsLazyPagingItems = netSearScriptList,
            modifier =modifier.padding(it),
            refreshFun = {
                searchViewModel.getRemoteScriptList(localScriptList)
            },
            listContent ={scriptInfo ->
                val processShow = remember {
                    mutableStateOf(false)
                }
                RowScriptInfo(
                    cardOnClick = {},
                    scriptInfo = scriptInfo,
                    surface = {
                      Box (
                          modifier = Modifier
                              .size(48.dp)
                              .clip(RoundedCornerShape(50))
                              .background(Color(color[Random.nextInt(0, color.size)])),
                          contentAlignment = Alignment.Center
                      ) {
                          Text(text = scriptInfo.scriptName.substring(0,1))
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
                                when (scriptInfo.downState.intValue) {
                                    1 -> Icon(painter = painterResource(id = R.drawable.baseline_download_done_24), contentDescription = null)
                                    2 -> Icon(painter = painterResource(id = R.drawable.baseline_downloading_24), contentDescription = null)
                                    else -> Icon(painter = painterResource(id = R.drawable.baseline_download_24), contentDescription = null)
                                }
                            },
                            onClick = {
                                if (scriptInfo.downState.intValue == 0){
                                    searchViewModel.viewModelScope.launch {
                                        scriptInfo.downState.intValue = 2
                                        processShow.value =true
                                        val res = searchViewModel.appViewModel.downScriptByScriptId( scriptInfo )
                                        if (!res){
                                            scriptInfo.downState.intValue = 0
                                            SnackbarUtil.show("下载失败，请联系管理员！")
                                        }
                                        //下载中
                                        processShow.value =false
                                        if(scriptInfo.downState.intValue == -1){
                                            scriptInfo.downState.intValue = 0
                                        }
                                    }
                                }
                            }
                        )
                    },
                    processShow = processShow
                )
            }
        )
    }
}