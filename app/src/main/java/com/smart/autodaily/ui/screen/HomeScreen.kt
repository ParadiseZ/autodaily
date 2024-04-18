package com.smart.autodaily.ui.screen

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.AlertDialog
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import com.smart.autodaily.R
import com.smart.autodaily.constant.Ui
import com.smart.autodaily.data.entity.ScriptInfo
import com.smart.autodaily.ui.conponent.SwipeRefreshList
import com.smart.autodaily.viewmodel.HomeViewMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier,
    nhc: NavHostController,
    homeViewModel : HomeViewMode
) {
    //弹窗
    val openDialog = remember { mutableStateOf(false) }
    val localScriptList = homeViewModel.getLocalScriptList().collectAsLazyPagingItems()
    SwipeRefreshList(
        collectAsLazyPagingItems = localScriptList,
        modifier =modifier,
        listContent ={
            RowList(
                scriptInfo = it,
                openDialog = openDialog,
                onclick = {
                    homeViewModel.checkBoxClick( it.is_downloaded,it )
                },
                isChecked = false,//homeViewModel.dataList[idx].checked_flag,
                onSmallRunClick = {
                    //homeViewModel.smallRunButtonClick( idx )
                }
            )
        }
    )
    //列表下拉刷新状态记录
    /*var refreshing  by remember {
        mutableStateOf(false)
    }*/
    /*val state = rememberPullRefreshState(refreshing = homeViewModel.refreshing.value, onRefresh = {
        //加载数据
        homeViewModel.viewModelScope.launch {
            homeViewModel.refreshing.value = true
            homeViewModel.loadLocalScriptInfo()
            delay(3000)
            homeViewModel.refreshing.value = false
        }
    })
    Box(modifier = modifier
        .pullRefresh(state)
    ){
        LazyColumn(
            modifier = Modifier
                .pullRefresh(state),
            contentPadding = PaddingValues(horizontal = Ui.SPACE_10, vertical = Ui.SPACE_5),
            verticalArrangement = Arrangement.spacedBy( Ui.SPACE_5 )
        ){
            this.itemsIndexed( homeViewModel. ){ idx, it->
                *//*val isChecked = remember {
                    mutableStateOf( it.isChecked )
                }*//*
                RowList(
                    scriptInfo = it,
                    openDialog = openDialog,
                    onclick = {
                        homeViewModel.checkBoxClick( idx,it )
                    },
                    isChecked = homeViewModel.dataList[idx].checked_flag,
                    onSmallRunClick = { homeViewModel.smallRunButtonClick( idx ) }
                )
            }
        }
        //下拉刷新更新数据
        PullRefreshIndicator(homeViewModel.refreshing.value, state, Modifier.align(Alignment.TopCenter))
    }*/
}

/*
* 脚本列表单行内容
* */
@Composable
fun RowList(
    scriptInfo: ScriptInfo,
    openDialog: MutableState<Boolean>,
    onclick: ()->Unit = {},
    isChecked: Boolean,
    onSmallRunClick : ()->Unit = {}
){
    Card(
        modifier = Modifier
            .clickable {
                //onItemClicked(item)
                onclick()
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
                //多选框
/*                var checkboxState by remember {
                    mutableStateOf( scriptInfo.isChecked )
                }*/
                Checkbox(checked = isChecked, onCheckedChange = {
                    onclick()
                })
                //图标信息
                Image(
                    painter = painterResource( id = R.drawable.bh3_offi),
                    contentDescription = null,
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(20))
                        .size(Ui.IMG_SIZE_50)
                )
                //脚本名称信息等
                Column(){
                    Text(text = scriptInfo.script_name, fontSize = Ui.SIZE_16 )
                    Text(text = scriptInfo.script_version, fontSize = Ui.SIZE_10 )
                }
            }

            Row (
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy( Ui.SPACE_5),
            ){
                //IconButton(imageVector = Icons.Outlined.PlayArrow, contentDescription = "运行")
                //更多
                IconButton(
                    modifier = Modifier
                        .border(
                            width = Ui.SPACE_1,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(50)
                        )
                        .size(Ui.ICON_SIZE_40),
                    content ={
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = null
                        )
                    },
                    onClick = {
                    }
                )
                //信息
                IconButton(
                    modifier = Modifier
                        .border(
                            width = Ui.SPACE_1,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(50)
                        )
                        .size(Ui.ICON_SIZE_40),
                    content ={
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null
                        )
                    },
                    onClick = {
                    }
                )
                //运行
                IconButton(
                    modifier = Modifier
                        .border(
                            width = Ui.SPACE_1,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(50)
                        )
                        .size(Ui.ICON_SIZE_40),
                    content ={
                        Icon(
                            imageVector = Icons.Outlined.PlayArrow,
                            contentDescription = null
                        )
                    },
                    onClick = onSmallRunClick
                )
                Spacer(modifier = Modifier.width( Ui.SPACE_5 ))
            }
        }
    }
    if (openDialog.value){
        AlertDialog(
            onDismissRequest = {
                openDialog.value = false
            },
            title = {
                Text(
                    text = "提示",
                    fontWeight = FontWeight.W700,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            text = {
                Text(
                    text = "这将意味着，我们会给您提供精准的位置服务，并且您将接受关于您订阅的位置信息",
                    fontSize = Ui.SIZE_16
                )
            },
            buttons = {
                Row(
                    modifier = Modifier
                        .padding(all = Ui.SPACE_10)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer
                        ),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        //modifier = Modifier.fillMaxWidth(),
                        modifier = Modifier.background(
                            color =  MaterialTheme.colorScheme.primaryContainer
                        ),
                        onClick = { openDialog.value = false }
                    ) {
                        Text(text = "必须接受！",modifier = Modifier.background(
                            color =  MaterialTheme.colorScheme.primaryContainer
                        ))
                    }
                    Button(
                        //modifier = Modifier.fillMaxWidth(),
                        modifier = Modifier.background(
                            color =  MaterialTheme.colorScheme.primaryContainer
                        ),
                        onClick = { openDialog.value = false }
                    ) {
                        Text(text = "取消")
                    }
                }
            }
        )
    }
}
val  show = { context : Context,message: Any ->
    Toast.makeText(context, message.toString(), Toast.LENGTH_SHORT).show()
}