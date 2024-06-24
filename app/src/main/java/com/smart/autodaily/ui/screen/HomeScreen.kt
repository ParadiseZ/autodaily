package com.smart.autodaily.ui.screen

import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
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
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.smart.autodaily.constant.AppBarTitle
import com.smart.autodaily.constant.NavigationItem
import com.smart.autodaily.constant.Ui
import com.smart.autodaily.data.appDb
import com.smart.autodaily.data.entity.ScriptInfo
import com.smart.autodaily.handler.RunScript
import com.smart.autodaily.ui.conponent.RowScriptInfo
import com.smart.autodaily.ui.conponent.Toast
import com.smart.autodaily.ui.conponent.navSingleTopTo
import com.smart.autodaily.utils.ScreenCaptureUtil
import com.smart.autodaily.utils.ServiceUtil
import com.smart.autodaily.utils.ShizukuUtil
import com.smart.autodaily.utils.ToastUtil
import com.smart.autodaily.viewmodel.HomeViewModel
import com.smart.autodaily.viewmodel.mediaProjectionServiceStartFlag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import splitties.init.appCtx

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier,
    nhc: NavHostController,
    homeViewModel : HomeViewModel = viewModel()
) {
    //弹窗
    val openDialog = remember { mutableStateOf(false) }
    //加载本地数据
    val localScriptList by homeViewModel.appViewModel.localScriptListAll.collectAsState()
    val needActiveList by homeViewModel.invalidScriptList.collectAsState()
    val user by homeViewModel.appViewModel.user.collectAsState()
    //currentNeedActiveListIndex
    val curNeedAcListIdx  by homeViewModel.curNeedAcListIdx.collectAsState()
    //showActiveDialogFlag
    val showActiveDialogFlag by homeViewModel.showActiveDialogFlag.collectAsState()
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
                    if(Build.VERSION.SDK_INT <=  Build.VERSION_CODES.S_V2){
                        accessibilityAndMediaProjectionRequest()
                    }
                    if (Build.VERSION.SDK_INT >=  Build.VERSION_CODES.TIRAMISU){
                        ServiceUtil.runUserService(homeViewModel.context)
                        if(ShizukuUtil.grant && ShizukuUtil.iUserService !=null){
                            homeViewModel.viewModelScope.launch {
                                RunScript.initScriptData(appDb!!.scriptInfoDao.getAllScriptByChecked())
                                RunScript.runScript()
                                /*for (i in 1..3){
                                    //RunScript.runScript(i)
                                    RunScript.runScript(1)
                                    delay(2000)
                                }*/
                            }
                        }
                    }

                    //}

                    /*val clickResult =homeViewModel.runButtonClick()
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
                                }else {
                                    if(!ServiceUtil.isAccessibilityServiceEnabled(homeViewModel.context)){
                                        ToastUtil.show(homeViewModel.context, "请先开启无障碍服务!")
                                        startActivity(
                                            homeViewModel.context,
                                            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                                            null
                                        )
                                    }
                                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                                        homeViewModel.appViewModel.runScript()
                                    }else{
                                        if(ScreenCaptureUtil.mps !=null){
                                            homeViewModel.appViewModel.runScript()
                                        } else {//未开启则引导开启
                                            (ScreenCaptureUtil.mediaProjectionDataMap["startActivityForResultLauncher"] as ActivityResultLauncher<Intent>).launch(
                                                ScreenCaptureUtil.mediaProjectionDataMap["mediaProjectionIntent"] as Intent
                                            )
                                            //ScreenCaptureUtil.mediaProjectionDataMap["resolver"]=homeViewModel.context.contentResolver//用来测试图片保存
                                        }
                                    }
                                }
                            }
                        }*/


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
        if (openDialog.value){
            AlertDialog(
                onDismissRequest = {
                    openDialog.value = false
                },
                confirmButton = {
                    OutlinedButton(
                        enabled = openDialog.value,
                        onClick = {
                            currentScriptInfo?.let { scriptInfo->
                                homeViewModel.deleteScript(scriptInfo)
                            }
                            openDialog.value = false
                            Toast.makeText(homeViewModel.context, "删除成功！", Toast.LENGTH_SHORT).show()
                        }
                    ){
                        Text(text = "确定")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        enabled = openDialog.value,
                        onClick = {
                            openDialog.value = false
                        }
                    ){
                        Text(text = "取消")
                    }
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
                        text = "您确定要删除它吗？",
                        fontSize = Ui.SIZE_16
                    )
                },
            )
        }
        if (showActiveDialogFlag && curNeedAcListIdx < needActiveList.size){
            AlertDialog(
                onDismissRequest = {
                    homeViewModel.cancelActiveScript(curNeedAcListIdx)
                },
                confirmButton = {
                    OutlinedButton(
                        onClick = {
                            homeViewModel.viewModelScope.launch {
                                val res = homeViewModel.activeScriptById(curNeedAcListIdx)
                                if (res.code!=200){
                                    res.message?.let { it1 ->
                                        ToastUtil.show(homeViewModel.context,
                                            it1
                                        )
                                    }
                                }
                            }
                        }
                    ){
                        Text(text = "激活")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = {
                            homeViewModel.cancelActiveScript(curNeedAcListIdx)
                        }
                    ){
                        Text(text = "取消")
                    }
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
                        text = "${needActiveList[curNeedAcListIdx].scriptName}已到期，是否花费1次机会激活它？剩余次数：${user?.canActivateNum}",
                        fontSize = Ui.SIZE_16
                    )
                },
            )
        }
    }
}


private fun accessibilityAndMediaProjectionRequest() {
    if (!ServiceUtil.isAccessibilityServiceEnabled(appCtx)) {
        ToastUtil.show(appCtx, "请先开启无障碍服务!")
        startActivity(
            appCtx,
            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            null
        )

        //runCheckScope.cancel().takeIf { runCheckScope.isActive }
        MainScope().launch {
            for (time in 1..10) {
                if (ServiceUtil.isAccessibilityServiceEnabled(appCtx)) {
                    if (ScreenCaptureUtil.mps != null) {
                        //homeViewModel.appViewModel.runScript()
                        //ScreenCaptureUtil.screenCapture()
                        //break
                    } else {
                        (ScreenCaptureUtil.mediaProjectionDataMap["startActivityForResultLauncher"] as ActivityResultLauncher<Intent>).launch(
                            ScreenCaptureUtil.mediaProjectionDataMap["mediaProjectionIntent"] as Intent
                        )
                        ScreenCaptureUtil.mediaProjectionDataMap["resolver"] =
                            appCtx.contentResolver//用来测试图片保存

                        break
                    }
                }
                delay(1000)
            }
        }


        MainScope().launch {
            for (time in 1..20) {
                if (mediaProjectionServiceStartFlag.value){
                    //ScreenCaptureUtil.screenCapture()
                    //ScreenCaptureU.getScreenshot(homeViewModel.context)

                    //ScreenCaptureUtil.screenCapture()
                    ScreenCaptureUtil.screenCaptureTIRAMISU()
                    break
                }
                delay(2000)

            }
        }
    }
}
