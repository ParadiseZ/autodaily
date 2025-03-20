package com.smart.autodaily.ui.screen

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import com.smart.autodaily.constant.AppBarTitle
import com.smart.autodaily.constant.BorderDirection
import com.smart.autodaily.constant.PermissionSettingText
import com.smart.autodaily.constant.SettingType
import com.smart.autodaily.constant.Ui
import com.smart.autodaily.data.appDb
import com.smart.autodaily.handler.isRunning
import com.smart.autodaily.ui.conponent.CheckBoxSettingItem
import com.smart.autodaily.ui.conponent.RadioButtonSettingItem
import com.smart.autodaily.ui.conponent.SingleBorderBox
import com.smart.autodaily.ui.conponent.SliderSecondSettingItem
import com.smart.autodaily.ui.conponent.SliderSettingItem
import com.smart.autodaily.ui.conponent.SwitchSettingItem
import com.smart.autodaily.ui.conponent.TextFieldSettingItem
import com.smart.autodaily.ui.conponent.TitleSettingItem
import com.smart.autodaily.viewmodel.SettingViewModel
import kotlinx.coroutines.launch
import splitties.init.appCtx


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    modifier: Modifier,
    nhc: NavHostController,
    settingViewModel: SettingViewModel = viewModel(),
) {
    val scriptSetLocalList = settingViewModel.getGlobalSetting().collectAsLazyPagingItems()
    val hasNewVer by settingViewModel.hasNewVer.collectAsState()
    var floatWindowFlag by remember { mutableStateOf(false) }
    val  newDialog = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        floatWindowFlag = Settings.canDrawOverlays(appCtx)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { granted ->
            floatWindowFlag = Settings.canDrawOverlays(appCtx)
        }
    )
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = AppBarTitle.SETTING_SCREEN) },
            )
        }
    ) { paddingValues ->
        /*var accessibilityServiceOpenFlagOld = false
        val accessibilityServiceOpenFlagNew = remember {
            mutableStateOf(
                ServiceUtil.isAccessibilityServiceEnabled(settingViewModel.context)
            )
        }*/

        var drownDownExpan by remember { mutableStateOf(false) }
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 10.dp)
                .fillMaxSize()
                //.padding(vertical = Ui.SPACE_8),
            //state = rememberLazyListState()
        ) {
            item {
                Spacer(modifier = Modifier.padding(vertical = Ui.SPACE_5))
            }
            //全局脚本设置模块
            item {
                SingleBorderBox(direction = BorderDirection.BOTTOM){
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Row{
                            Text("全局设置",fontWeight = FontWeight.Bold)
                            if (hasNewVer){
                                Spacer(modifier = Modifier.padding(start = Ui.SPACE_5))
                                Text("New",fontSize = Ui.SIZE_10 , color = Color.Red)
                            }
                        }
                        IconButton(onClick = {
                            drownDownExpan = !drownDownExpan
                        }){
                            Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                contentDescription = null
                            )
                            DropdownMenu(
                                expanded = drownDownExpan,
                                onDismissRequest = { drownDownExpan = false },
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Icon(
                                            imageVector = Icons.Outlined.Refresh,
                                            contentDescription = null
                                        )
                                        Text(text = "更新")
                                    },
                                    onClick = {
                                        drownDownExpan = false
                                        settingViewModel.appViewModel.stopRunScript()
                                        newDialog.value = !newDialog.value
                                    }
                                )
                            }
                        }

                    }
                }
            }
            item {
                Spacer(modifier = Modifier.padding(vertical = Ui.SPACE_5))
            }
            if (scriptSetLocalList.itemCount == 0) {
                item {
                    Text(text = "空空如也，请先去下载脚本！")
                }
            } else {
                items(scriptSetLocalList.itemCount) { index ->
                    scriptSetLocalList[index]?.let { setting ->
                        when (setting.setType) {
                            SettingType.SWITCH -> SwitchSettingItem(
                                setting,
                                onSwitchChange = { settingViewModel.updateGlobalSetting(setting) })
                            //slider无step、百分比
                            SettingType.SLIDER -> SliderSettingItem(
                                setting,
                                onSliderValueChange = {
                                    settingViewModel.updateGlobalSetting(setting)
                                })

                            SettingType.TEXT_FIELD -> TextFieldSettingItem(
                                setting,
                                onValueChange = { settingViewModel.updateGlobalSetting(setting) })

                            SettingType.CHECK_BOX -> CheckBoxSettingItem(
                                setting,
                                onCheckedChange = { settingViewModel.updateGlobalSetting(setting) })

                            SettingType.RADIO_BUTTON -> RadioButtonSettingItem(
                                setting,
                                onCheckedChange = { settingViewModel.updateGlobalSetting(setting) })

                            SettingType.TITLE -> TitleSettingItem(setting.setName)

                            SettingType.DROPDOWN_MENU -> {}
                            else ->{
                                //slider类型、有step
                                SliderSecondSettingItem(
                                    setting,
                                    onSliderValueChange = {
                                        settingViewModel.updateGlobalSetting(setting)
                                    })
                            }
                        }
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.padding(vertical = Ui.SPACE_5))
            }
            item {
                //权限设置模块
                SingleBorderBox(direction = BorderDirection.BOTTOM){
                    Row (modifier = Modifier.fillMaxWidth()){
                        Text("权限设置", fontWeight = FontWeight.Bold)
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.padding(vertical = Ui.SPACE_5))
            }
            /*
                item {
                    RowSwitchPermission(
                        labelText = PermissionSettingText.ACCESSBILITY_SERVICE_TEXT,
                        isSwitchOpen = accessibilityServiceOpenFlagNew,
                        onSwitchChange = {
                            startActivity(
                                settingViewModel.context,
                                Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                                null
                            )
                            settingViewModel.viewModelScope.launch {
                                for (i in 1..30) {//检测30秒内是否开启成功
                                    delay(1000)
                                    accessibilityServiceOpenFlagNew.value =
                                        ServiceUtil.isAccessibilityServiceEnabled(settingViewModel.context)
                                    if (accessibilityServiceOpenFlagOld != accessibilityServiceOpenFlagNew.value) {
                                        accessibilityServiceOpenFlagOld =
                                            accessibilityServiceOpenFlagNew.value
                                        break
                                    }
                                }
                            }
                        })
                }
                item {
                    RowSwitchPermission(PermissionSettingText.SCREEN_RECORD_TEXT,
                        isSwitchOpen = mediaProjectionServiceStartFlag,
                        onSwitchChange = {
                            if (mediaProjectionServiceStartFlag.value) {
                                settingViewModel.context.also {
                                    it.stopService(Intent(it, MediaProjectionService::class.java))
                                }
                            } else {//未开启则引导开启
                                (ScreenCaptureUtil.mediaProjectionDataMap["startActivityForResultLauncher"] as ActivityResultLauncher<Intent>).launch(
                                    ScreenCaptureUtil.mediaProjectionDataMap["mediaProjectionIntent"] as Intent
                                )
                                ScreenCaptureUtil.mediaProjectionDataMap["resolver"] =
                                    settingViewModel.context.contentResolver//用来测试图片保存
                            }
                        })
                }
                */
            item {
                RowIconButtonPermission(PermissionSettingText.IGNORE_BATTERIES_TEXT,
                    iconButtonOnClick = {
                        launcher.launch(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                    })
            }
            item {
                RowSwitchPermission(PermissionSettingText.FLOAT_WINDOW_TEXT,
                    isSwitchOpen = floatWindowFlag,
                    onSwitchChange = {
                        launcher.launch(
                            Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                        )
                    }
                )
            }
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
                            settingViewModel.viewModelScope.launch{
                                if(isRunning.intValue == 1){
                                    settingViewModel.appViewModel.stopRunScript()
                                }
                                settingViewModel.appViewModel.getScriptInfoGlobal()?.let {
                                    try {
                                        settingViewModel.deleteScript()
                                        settingViewModel.appViewModel.downScriptByScriptId(it)
                                        settingViewModel.getGlobalSetting()
                                        snackbarHostState.showSnackbar("更新成功！")
                                    }catch (e : Exception){
                                        appDb.scriptInfoDao.insert(it)
                                        snackbarHostState.showSnackbar("更新失败，请稍后重试！")
                                    }
                                }
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

@Composable
fun RowSwitchPermission(
    labelText: String,
    isSwitchOpen: Boolean,
    onSwitchChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = Ui.SPACE_8, vertical = Ui.SPACE_5)
            .clickable {
                onSwitchChange(!isSwitchOpen)
            },
        verticalAlignment = Alignment.CenterVertically
    ){
        Text(
            modifier = Modifier.weight(1f),
            text = labelText
        )
        Switch(checked = isSwitchOpen, onCheckedChange = {
            onSwitchChange(it)
        })
    }
}

@Composable
fun RowIconButtonPermission(
    labelText: String,
    iconButtonOnClick: () -> Unit,
){
    Row(
        modifier = Modifier
            .padding(horizontal = Ui.SPACE_8, vertical = Ui.SPACE_5)
            .clickable { iconButtonOnClick() },
        verticalAlignment = Alignment.CenterVertically
    ){
        Text(
            modifier = Modifier.weight(1f),
            text = labelText
        )
        IconButton(onClick = { iconButtonOnClick() }) {
            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        }
    }
}
