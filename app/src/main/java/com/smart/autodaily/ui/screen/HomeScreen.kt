package com.smart.autodaily.ui.screen

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.AlertDialog
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Button
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import com.smart.autodaily.constant.ScreenText
import com.smart.autodaily.constant.Ui
import com.smart.autodaily.data.entity.ScriptInfo
import com.smart.autodaily.ui.conponent.IconButtonCustom
import com.smart.autodaily.ui.conponent.RowListCustom
import com.smart.autodaily.ui.conponent.SearchTopAppBar
import com.smart.autodaily.ui.conponent.SwipeRefreshList
import com.smart.autodaily.viewmodel.HomeViewMode

@Composable
fun HomeScreen(
    modifier: Modifier,
    nhc: NavHostController,
    homeViewModel : HomeViewMode = viewModel()
) {
    //弹窗
    val openDialog = remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    val localScriptList = homeViewModel.getLocalScriptList(searchText).collectAsLazyPagingItems()
    Scaffold (
        modifier = modifier,
        topBar = {
            SearchTopAppBar(searchButtonText = ScreenText.SEARCH_SCREEN, onSearchClick = {
                searchText = it
            })
        },
        //脚本页面运行按钮
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    homeViewModel.runButtonClick()
                }
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = "开始运行")
            }
        },
    ){
        SwipeRefreshList(
            collectAsLazyPagingItems = localScriptList,
            modifier =modifier.padding(it),
            listContent ={ scriptInfo ->
                var checkedFlag by remember { mutableStateOf(scriptInfo.checked_flag) }
                RowListCustom(
                    cardOnClick = {},
                    scriptInfo = scriptInfo,
                    checkBox = {
                        Checkbox(checked = checkedFlag, onCheckedChange = {
                            checkedFlag = !checkedFlag
                        })
                    },
                    iconInfo ={
                        IconButtonCustom(icon = Icons.Outlined.MoreVert)
                        IconButtonCustom(icon = Icons.Outlined.Info)
                        IconButtonCustom(icon = Icons.Outlined.PlayArrow)
                    }
                )
            }
        )
    }
}

/*
* 脚本列表单行内容
* */

    /*if (openDialog.value){
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
}*/

