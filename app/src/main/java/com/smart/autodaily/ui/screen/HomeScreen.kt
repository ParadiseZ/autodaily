package com.smart.autodaily.ui.screen

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.smart.autodaily.constant.AppBarTitle
import com.smart.autodaily.constant.ResponseCode
import com.smart.autodaily.constant.Ui
import com.smart.autodaily.data.entity.ScriptInfo
import com.smart.autodaily.data.entity.resp.Response
import com.smart.autodaily.handler.RunScript
import com.smart.autodaily.handler.isRunning
import com.smart.autodaily.ui.conponent.RowScriptInfo
import com.smart.autodaily.utils.ScreenCaptureUtil
import com.smart.autodaily.utils.ServiceUtil
import com.smart.autodaily.utils.ToastUtil
import com.smart.autodaily.utils.isLogin
import com.smart.autodaily.utils.runScope
import com.smart.autodaily.utils.toastOnUi
import com.smart.autodaily.viewmodel.HomeViewModel
import com.smart.autodaily.viewmodel.mediaProjectionServiceStartFlag
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import splitties.init.appCtx
import java.io.InterruptedIOException
import kotlin.system.exitProcess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier,
    nhc: NavHostController,
    homeViewModel : HomeViewModel = viewModel()
) {
    //删除弹窗
    val openDialog = remember { mutableStateOf(false) }
    //更新弹窗
    val  newDialog = remember { mutableStateOf(false) }
    //退出确认弹窗
    val lastBackTime = remember { mutableLongStateOf(0L) }
    //加载本地数据
    val localScriptList by homeViewModel.appViewModel.localScriptListAll.collectAsState()
    val user by homeViewModel.appViewModel.user.collectAsState()
    var currentScriptInfo : ScriptInfo? = null
    //script运行状态
    val runStatus by isRunning
    //提示信息设置
    val snackbarHostState = remember { SnackbarHostState() }
    //设置详细展开、提示信息设置公用
    val scope = rememberCoroutineScope()
    
    // 处理返回键事件
    BackHandler {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastBackTime.longValue < 2000) {
            // 两次点击间隔小于2秒，退出应用
            exitProcess(0)
        } else {
            // 更新上次点击时间并提示用户
            lastBackTime.longValue = currentTime
            scope.launch {
                snackbarHostState.showSnackbar("再按一次退出")
            }
        }
    }
    
    Scaffold (
        modifier = modifier,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
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
            when(runStatus){
                1->{
                    FloatingActionButton (
                        shape = MaterialTheme.shapes.medium.copy(CornerSize(percent = 50)),
                        onClick = {
                            homeViewModel.viewModelScope.launch {
                                homeViewModel.appViewModel.stopRunScript()
                            }
                        }
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "stop")
                    }
                }
                0->{
                    FloatingActionButton (
                        shape = MaterialTheme.shapes.medium.copy(CornerSize(percent = 50)),
                        onClick = {
                            runScope.launch {
                                /*homeViewModel.appViewModel.setIsRunning(2)
                                RunScript.initGlobalSet()
                                homeViewModel.appViewModel.runScript()
                                homeViewModel.appViewModel.setIsRunning(2)*/
                                if(isLogin(homeViewModel.context, user)){
                                    val res : Response<String>
                                    try {
                                        res =  homeViewModel.runScriptCheck()
                                    }catch(e : InterruptedIOException){
                                        appCtx.toastOnUi("连接服务器异常！")
                                        homeViewModel.appViewModel.setIsRunning(0)
                                        return@launch
                                    }
                                    if(scriptCheckResHand(res =res)){
                                        RunScript.initGlobalSet()
                                        homeViewModel.appViewModel.runScript()
                                    }else{
                                        homeViewModel.appViewModel.setIsRunning(0)
                                    }
                                }else{
                                    homeViewModel.appViewModel.setIsRunning(0)
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = "start")
                    }
                }
                else->{}
            }
        },
    ){
        LazyColumn(
            modifier = modifier.padding(it)
        ) {
            if(localScriptList.isNotEmpty()) {
                items(localScriptList) { scriptInfo ->
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
                        iconInfo = {
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
                                            Row {
                                                Icon(
                                                    imageVector = Icons.Outlined.Settings,
                                                    contentDescription = null
                                                )
                                                Text(text = "设置")
                                            }
                                        },
                                        onClick = {
                                            dropdownIsOpen = false
                                            currentScriptInfo = scriptInfo
                                            appCtx.startActivity(
                                                Intent("android.intent.action.SCRIPT SET DETAIL")
                                                    .putExtra("CUR_SCRIPT_ID", scriptInfo.scriptId)
                                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            )
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Row {
                                                Icon(
                                                    imageVector = Icons.Outlined.Refresh,
                                                    contentDescription = null
                                                )
                                                Text(text = "更新")
                                            }
                                        },
                                        onClick = {
                                            currentScriptInfo = scriptInfo
                                            dropdownIsOpen = false
                                            homeViewModel.appViewModel.stopRunScript()
                                            newDialog.value = !newDialog.value
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Row {
                                                Icon(
                                                    imageVector = Icons.Outlined.Delete,
                                                    contentDescription = null
                                                )
                                                Text(text = "删除运行记录")
                                            }
                                        },
                                        onClick = {
                                            currentScriptInfo = scriptInfo
                                            dropdownIsOpen = false
                                            homeViewModel.appViewModel.stopRunScript()
                                            scope.launch {
                                                homeViewModel.deleteRunStatus(scriptInfo)
                                                snackbarHostState.showSnackbar("删除运行记录成功！")
                                            }
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Row {
                                                Icon(
                                                    imageVector = Icons.Outlined.Delete,
                                                    contentDescription = null
                                                )
                                                Text(text = "删除")
                                            }
                                        },
                                        onClick = {
                                            currentScriptInfo = scriptInfo
                                            dropdownIsOpen = false
                                            homeViewModel.appViewModel.stopRunScript()
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
            }else{
                item {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                        Text(text = "嗯......空空如也")
                    }
                }
            }
        }
        if (openDialog.value){
            AlertDialog(
                onDismissRequest = {
                    openDialog.value = false
                },
                confirmButton = {
                    TextButton(
                        enabled = openDialog.value,
                        onClick = {
                            currentScriptInfo?.let { scriptInfo->
                                homeViewModel.deleteScript(scriptInfo)
                            }
                            openDialog.value = false
                            scope.launch {
                                snackbarHostState.showSnackbar("删除成功！")
                            }
                        }
                    ){
                        Text(text = "确定")
                    }
                },
                dismissButton = {
                    TextButton(
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

        if (newDialog.value){
            AlertDialog(
                onDismissRequest = {
                    newDialog.value = false
                },
                confirmButton = {
                    OutlinedButton(
                        enabled = newDialog.value,
                        onClick = {
                            homeViewModel.viewModelScope.launch {
                                if (currentScriptInfo!=null){
                                    try {
                                        homeViewModel.deleteScript(currentScriptInfo)
                                        homeViewModel.appViewModel.downScriptByScriptId(currentScriptInfo)
                                    }catch (e:Exception){
                                        snackbarHostState.showSnackbar("更新失败！请重新下载！")
                                    }
                                }else{
                                    snackbarHostState.showSnackbar("更新成功！")
                                }
                                newDialog.value = false
                            }
                        }
                    ){
                        Text(text = "确定")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        enabled = newDialog.value,
                        onClick = {
                            newDialog.value = false
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
                        text = "您确定要更新吗？",
                        fontSize = Ui.SIZE_16
                    )
                },
            )
        }
    }
}

private fun scriptCheckResHand(context: Context= appCtx , res : Response<String>) : Boolean{
    if (res.code==ResponseCode.SUCCESS_OK.code){
        return true
    }
    context.toastOnUi(res.message)
    return false
}

private fun accessibilityAndMediaProjectionRequest() {
    if (!ServiceUtil.isAccessibilityServiceEnabled(appCtx)) {
        ToastUtil.show(appCtx, "请先开启无障碍服务!")
        appCtx.startActivity(
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