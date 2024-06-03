package com.smart.autodaily.ui.screen

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.smart.autodaily.constant.AppBarTitle
import com.smart.autodaily.constant.NavigationItem
import com.smart.autodaily.constant.RunButtonClickResult
import com.smart.autodaily.constant.Ui
import com.smart.autodaily.data.entity.ScriptInfo
import com.smart.autodaily.ui.LoginActivity
import com.smart.autodaily.ui.conponent.RowScriptInfo
import com.smart.autodaily.ui.conponent.navSingleTopTo
import com.smart.autodaily.utils.ToastUtil
import com.smart.autodaily.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier,
    nhc: NavHostController,
    homeViewModel : HomeViewModel = viewModel(),
) {
    //弹窗
    val openDialog = remember { mutableStateOf(false) }
    //加载本地数据
    val localScriptList by homeViewModel.appViewModel.localScriptListAll.collectAsState()
    var currentScriptInfo : ScriptInfo? = null
    Scaffold (
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = AppBarTitle.HOME_SCREEN)
                }
            )
        },
        //脚本页面运行按钮
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    homeViewModel.viewModelScope.launch{
                        val clickResult =homeViewModel.runButtonClick()
                        when(clickResult){
                            RunButtonClickResult.NOT_LOGIN->{
                                startActivity(homeViewModel.context,
                                    Intent(homeViewModel.context, LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                                    null
                                )
                            }
                            RunButtonClickResult.LOGIN_SUCCESS->{
                                val clickResult = homeViewModel.runScriptCheck()
                                if (clickResult.code!=200){
                                    ToastUtil.show(homeViewModel.context, clickResult.message.toString())
                                }
                            }
                        }
                    }
                }
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = "开始运行")
            }
        },
    ){
        LazyColumn(
            modifier = modifier.padding(it)
        ) {
            items(localScriptList){
                scriptInfo ->
                var checkedFlag by remember { mutableStateOf(scriptInfo.checkedFlag) }
                RowScriptInfo(
                    cardOnClick = {
                        checkedFlag = !checkedFlag
                        scriptInfo.checkedFlag = checkedFlag
                        homeViewModel.appViewModel.updateScript(scriptInfo)
                    },
                    scriptInfo = scriptInfo,
                    checkBox = {
                        Checkbox(checked = checkedFlag, onCheckedChange = {
                            checkedFlag = !checkedFlag
                            scriptInfo.checkedFlag = checkedFlag
                            homeViewModel.appViewModel.updateScript(scriptInfo)
                        })
                    },
                    iconInfo ={
                        var dropdownIsOpen by remember { mutableStateOf(false) }
                        IconButton(
                            onClick = {
                                dropdownIsOpen = !dropdownIsOpen
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                contentDescription = null
                            )
                            DropdownMenu(
                                expanded = dropdownIsOpen,
                                onDismissRequest = { dropdownIsOpen = false },
                                // 使用offset控制DropdownMenu的显示位置，基于按钮的位置和大小动态计算
                            ) {
                                // DropdownMenu的内容
                                DropdownMenuItem(
                                    text = {
                                        Row{
                                            Icon(imageVector = Icons.Outlined.Settings, contentDescription = null)
                                            Text(text = "设置")
                                        }
                                    },
                                    onClick = {
                                        currentScriptInfo = scriptInfo
                                        nhc.navSingleTopTo(NavigationItem.PERSON.route)
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Row{
                                            Icon(imageVector = Icons.Outlined.Refresh, contentDescription = null)
                                            Text(text = "更新")
                                        }
                                    },
                                    onClick = { /* 处理选项被点击的逻辑 */ }
                                )
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
                                        currentScriptInfo = scriptInfo
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
        }
        /*SwipeRefreshList(
            collectAsLazyPagingItems = localScriptList,
            modifier =modifier.padding(it),
            listContent ={ scriptInfo ->


            }
        )*/
        if (openDialog.value){
            BasicAlertDialog(
                properties = DialogProperties(),
                onDismissRequest = {
                    openDialog.value = false
                },
                content = {
                    Text(
                        text = "确认操作",
                        fontWeight = FontWeight.W700,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "您确定要删除该脚本吗？",
                        fontSize = Ui.SIZE_16
                    )

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
                                currentScriptInfo?.let { scriptInfo->
                                    homeViewModel.deleteScript(scriptInfo) }
                                Toast.makeText(homeViewModel.context, "删除成功！", Toast.LENGTH_SHORT).show()
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

