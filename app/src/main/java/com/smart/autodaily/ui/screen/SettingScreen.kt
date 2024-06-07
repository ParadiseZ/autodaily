package com.smart.autodaily.ui.screen

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import com.smart.autodaily.constant.AppBarTitle
import com.smart.autodaily.constant.PermissionSettingText
import com.smart.autodaily.constant.SettingTitle
import com.smart.autodaily.constant.Ui
import com.smart.autodaily.data.entity.SettingType
import com.smart.autodaily.service.MediaProjectionService
import com.smart.autodaily.ui.conponent.CheckBoxSettingItem
import com.smart.autodaily.ui.conponent.RadioButtonSettingItem
import com.smart.autodaily.ui.conponent.SliderSecondSettingItem
import com.smart.autodaily.ui.conponent.SliderSettingItem
import com.smart.autodaily.ui.conponent.SwitchSettingItem
import com.smart.autodaily.ui.conponent.TextFieldSettingItem
import com.smart.autodaily.utils.ScreenCaptureUtil
import com.smart.autodaily.utils.ServiceUtil
import com.smart.autodaily.viewmodel.SettingViewModel
import com.smart.autodaily.viewmodel.mediaProjectionServiceStartFlag
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    modifier: Modifier,
    nhc: NavHostController,
    settingViewModel: SettingViewModel = viewModel(),
) {
    val scriptSetLocalList = settingViewModel.getGlobalSetting().collectAsLazyPagingItems()
    Scaffold (
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {    Text(text = AppBarTitle.SETTING_SCREEN)   },
            )
        }
    ){  paddingValues ->
        val sharedPreferences = remember {
            settingViewModel.context.getSharedPreferences("permission_setting", Context.MODE_PRIVATE)
        }
        val firstSettingOpenFlag = remember { mutableStateOf(
            sharedPreferences.getBoolean("first_setting_expand", false)
        ) }
        val secondSettingOpenFlag = remember { mutableStateOf(
            sharedPreferences.getBoolean("second_setting_expand", false)
        ) }
        Column(modifier = Modifier
            .padding(paddingValues)
            .fillMaxWidth()){
            //全局脚本设置模块
            CardCustom(firstSettingOpenFlag,SettingTitle.SETTING_GLOBAL,onClick = {
                sharedPreferences.edit().putBoolean("first_setting_expand", it).apply()
            })
            if (firstSettingOpenFlag.value) {
                if (scriptSetLocalList.itemCount == 0) {
                    Column(modifier = Modifier.padding(Ui.SPACE_8)) {
                        Text(text = "空空如也，请先去下载脚本！")
                    }
                }else{
                    LazyColumn (
                        modifier = Modifier
                            .padding(vertical = Ui.SPACE_8),
                        state =  rememberLazyListState()
                    ){
                        items(scriptSetLocalList.itemCount) { index ->
                            scriptSetLocalList[index]?.let { setting ->
                                when (setting.setType) {
                                    SettingType.SWITCH -> SwitchSettingItem(setting,onSwitchChange = {  settingViewModel.updateGlobalSetting(setting)  })
                                    SettingType.SLIDER -> SliderSettingItem(setting,onSliderValueChange = {  settingViewModel.updateGlobalSetting(setting)  })
                                    SettingType.TEXT_FIELD -> TextFieldSettingItem(setting,onValueChange = {  settingViewModel.updateGlobalSetting(setting)  })
                                    SettingType.CHECK_BOX -> CheckBoxSettingItem(setting,onCheckedChange = {  settingViewModel.updateGlobalSetting(setting)  })
                                    SettingType.RADIO_BUTTON -> RadioButtonSettingItem(setting, onCheckedChange = {  settingViewModel.updateGlobalSetting(setting)  })
                                    SettingType.SLIDER_SECOND -> SliderSecondSettingItem(setting,onSliderValueChange = {  settingViewModel.updateGlobalSetting(setting)  })
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(Ui.SPACE_8))
            //权限设置模块
            CardCustom(secondSettingOpenFlag, SettingTitle.SETTING_PERMISSION,onClick = {
                sharedPreferences.edit().putBoolean("second_setting_expand", it).apply()
            })
            var accessibilityServiceOpenFlagOld = false
            val accessibilityServiceOpenFlagNew = remember {
                mutableStateOf(
                    ServiceUtil.isAccessibilityServiceEnabled(settingViewModel.context)
                )
            }

            val floatWindowFlag = remember { mutableStateOf(sharedPreferences.getBoolean("float_window", false)) }
            if (secondSettingOpenFlag.value) {
                RowSwitchPermission(
                    labelText =PermissionSettingText.ACCESSBILITY_SERVICE_TEXT,
                    isSwitchOpen = accessibilityServiceOpenFlagNew,
                    onSwitchChange = {
                        startActivity(
                            settingViewModel.context,
                            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                            null
                        )
                        settingViewModel.viewModelScope.launch {
                            for (i in 1..30){//检测30秒内是否开启成功
                                delay(1000)
                                accessibilityServiceOpenFlagNew.value = ServiceUtil.isAccessibilityServiceEnabled(settingViewModel.context)
                                if (accessibilityServiceOpenFlagOld != accessibilityServiceOpenFlagNew.value) {
                                    accessibilityServiceOpenFlagOld = accessibilityServiceOpenFlagNew.value
                                    break
                                }
                            }
                        }
                })
                RowSwitchPermission(PermissionSettingText.SCREEN_RECORD_TEXT,
                    isSwitchOpen = mediaProjectionServiceStartFlag,
                    onSwitchChange={
                        if(mediaProjectionServiceStartFlag.value){
                            settingViewModel.context.also {
                                it.stopService(Intent(it, MediaProjectionService::class.java))
                            }
                        } else {//未开启则引导开启
                            (ScreenCaptureUtil.mediaProjectionDataMap["startActivityForResultLauncher"] as ActivityResultLauncher<Intent>).launch(
                                ScreenCaptureUtil.mediaProjectionDataMap["mediaProjectionIntent"] as Intent
                            )
                            //ScreenCaptureUtil.mediaProjectionDataMap["resolver"]=settingViewModel.context.contentResolver//用来测试图片保存
                        }
                })
                RowIconButtonPermission(PermissionSettingText.IGNORE_BATTERIES_TEXT,
                    iconButtonOnClick  = {
                        startActivity(
                            settingViewModel.context,
                            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                            null)
                    })
                RowSwitchPermission(PermissionSettingText.FLOAT_WINDOW_TEXT,
                    isSwitchOpen = floatWindowFlag,
                    onSwitchChange = {
                        startActivity(
                            settingViewModel.context,
                            Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                            null)
                        settingViewModel.viewModelScope.launch {
                            var canDrawOverlays : Boolean
                            for (i in 1..30) {//检测30秒内是否开启成功
                                delay(1000)
                                canDrawOverlays= Settings.canDrawOverlays(settingViewModel.context)
                                if(canDrawOverlays != floatWindowFlag.value){
                                    floatWindowFlag.value = it
                                    sharedPreferences.edit().putBoolean("float_window", floatWindowFlag.value).apply()
                                    break
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}


@Composable
fun CardCustom(
    isExpanded: MutableState<Boolean>,
    labelText: String,
    onClick: (Boolean) -> Unit = {}
) {
    Card(
        onClick = {
            isExpanded.value = !isExpanded.value
            onClick(isExpanded.value)
        }
    ) {
        Row (
            modifier = Modifier.padding(Ui.SPACE_8),
            verticalAlignment = Alignment.CenterVertically
        ){
            Text(
                modifier = Modifier.weight(1f),
                text = labelText
            )
            IconButton(onClick = {
                isExpanded.value = !isExpanded.value
                onClick(isExpanded.value)
            }) {
                Icon(imageVector = (if (isExpanded.value) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowLeft)
                    , contentDescription = null)
            }
        }
    }
}

@Composable
fun RowSwitchPermission(
    labelText: String,
    isSwitchOpen: MutableState<Boolean>,
    onSwitchChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.padding(horizontal = Ui.SPACE_8,vertical = Ui.SPACE_4)
            .clickable {
                onSwitchChange(!isSwitchOpen.value)
            },
        verticalAlignment = Alignment.CenterVertically
    ){
        Text(
            modifier = Modifier.weight(1f),
            text = labelText
        )
        Switch(checked = isSwitchOpen.value, onCheckedChange = {
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
        modifier = Modifier.padding(horizontal = Ui.SPACE_8,vertical = Ui.SPACE_4)
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