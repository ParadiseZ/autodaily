package com.smart.autodaily.ui.screen

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier,
    nhc: NavHostController,
    homeViewModel : HomeViewMode = viewModel()
) {
    //弹窗
    val openDialog = remember { mutableStateOf(false) }
    val localScriptList = homeViewModel.getLocalScriptList().collectAsLazyPagingItems()
    var diagSc : ScriptInfo? = null
    Scaffold (
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = ScreenText.HOME_SCREEN)
                }
            )
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
                var expanded by remember { mutableStateOf(false) }
                RowListCustom(
                    cardOnClick = {
                        checkedFlag = !checkedFlag
                    },
                    scriptInfo = scriptInfo,
                    checkBox = {
                        Checkbox(checked = checkedFlag, onCheckedChange = {
                            checkedFlag = !checkedFlag
                        })
                    },
                    iconInfo ={
                        IconButton(
                            onClick = {
                                expanded = !expanded
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                contentDescription = null
                            )
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                // 使用offset控制DropdownMenu的显示位置，基于按钮的位置和大小动态计算
                            ) {
                                // DropdownMenu的内容
                                DropdownMenuItem(
                                    text = {
                                           Row{
                                               Icon(imageVector = Icons.Outlined.Share, contentDescription = null)
                                               Text(text = "分享")
                                           }
                                    },
                                    onClick = { /* 处理选项被点击的逻辑 */ }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Row{
                                            Icon(imageVector = Icons.Outlined.Delete, contentDescription = null)
                                            Text(text = "删除")
                                        }
                                    },
                                    onClick = {
                                        diagSc = scriptInfo
                                        openDialog.value = !openDialog.value
                                    }
                                )
                                // 根据需要添加更多选项
                            }
                        }
                        /*IconButtonCustom(icon = Icons.Outlined.MoreVert)
                        IconButtonCustom(icon = Icons.Outlined.Info)
                        IconButtonCustom(icon = Icons.Outlined.PlayArrow)*/
                    }
                )

            }
        )
        if (openDialog.value){
            AlertDialog(
                onDismissRequest = {
                    openDialog.value = false
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
                        text = "您确定要删除该脚本吗？",
                        fontSize = Ui.SIZE_16
                    )
                },
                buttons = {
                    Row(
                        modifier = Modifier
                            .padding(start = 24.dp, end = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = { openDialog.value = false }
                        ) {
                            Text(text = "取消")
                        }
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                openDialog.value = false
                                println("==="+diagSc.toString())
                                diagSc?.let { homeViewModel.deleteScript(it) }
                                Toast.makeText(homeViewModel.context, "删除成功！", Toast.LENGTH_SHORT).show()
                                localScriptList.refresh()
                            }
                        ) {
                            Text(text = "删除")
                        }
                    }
                }
            )
        }
    }
}

/*
* 脚本列表单行内容
* */



/*
val  show = { context : Context,message: Any ->
    Toast.makeText(context, message.toString(), Toast.LENGTH_SHORT).show()
}*/

