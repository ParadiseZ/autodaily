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
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.smart.autodaily.R
import com.smart.autodaily.constant.ScreenText
import com.smart.autodaily.constant.Ui
import com.smart.autodaily.data.entity.ScriptInfo
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
            SearchTopAppBar(searchButtonText = ScreenText.serachScreen, onSearchClick = {
                searchText = it
            })
        },
    ){
        SwipeRefreshList(
            collectAsLazyPagingItems = netSearScriptList,
            modifier =modifier.padding(it),
            listContent ={
                RowListSearch(it , downButtonClick = { searchViewModel.downScriptByScriptId( it ) })
            }
        )
    }
}
@Composable
fun RowListSearch(
    scriptInfo : ScriptInfo,
    downButtonClick : () -> Unit = {}
){
    var isDownloaded by remember {
        mutableStateOf(scriptInfo.is_downloaded)
    }
    //卡片
    Card(
        modifier = Modifier
            .clickable {
            },
    ){
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Ui.SPACE_5),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Row (
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy( Ui.SPACE_5   ),
                //modifier = Modifier.padding(vertical = Ui.SPACE_5)
            ){
                Spacer(modifier = Modifier.width( Ui.SPACE_5 ))
                //图标信息
                Text(text = scriptInfo.script_id.toString() )
                Image(
                    painter = painterResource( id = R.drawable.bh3_offi),
                    contentDescription = null,
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(20))
                        .size(Ui.IMG_SIZE_50)
                )
                //脚本名称信息等
                Column{
                    Text(text = scriptInfo.script_name, fontSize = Ui.SIZE_12 )
                    Text(text = scriptInfo.script_version, fontSize = Ui.SIZE_10 )
                }
            }
            //操作按钮
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy( Ui.SPACE_5),
            ) {
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
                            downButtonClick()
                            isDownloaded = 1
                        }
                    }
                )
            }
        }
    }
}