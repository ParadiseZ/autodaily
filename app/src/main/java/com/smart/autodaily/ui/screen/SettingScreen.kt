package com.smart.autodaily.ui.screen

import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint
import android.app.Notification
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import com.smart.autodaily.constant.AppBarTitle
import com.smart.autodaily.constant.ForegroundServiceId
import com.smart.autodaily.constant.PermissionSettingText
import com.smart.autodaily.constant.SettingTitle
import com.smart.autodaily.constant.Ui
import com.smart.autodaily.data.entity.SettingType
import com.smart.autodaily.service.AccessibilityService
import com.smart.autodaily.ui.conponent.CheckBoxSettingItem
import com.smart.autodaily.ui.conponent.RadioButtonSettingItem
import com.smart.autodaily.ui.conponent.SliderSettingItem
import com.smart.autodaily.ui.conponent.SwitchSettingItem
import com.smart.autodaily.ui.conponent.TextFieldSettingItem
import com.smart.autodaily.viewmodel.SettingViewModel
import com.smart.autodaily.viewmodel.mediaProjectionServiceStartFlag

@SuppressLint("ForegroundServiceType")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    modifier: Modifier,
    nhc: NavHostController,
    settingViewModel: SettingViewModel = viewModel()
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
        val firstSettingOpenFlag = remember { mutableStateOf(false) }
        val secondSettingOpenFlag = remember { mutableStateOf(false) }
        Column(modifier = Modifier
            .padding(paddingValues)
            .fillMaxWidth()){
            //全局脚本设置模块
            CardCustom(firstSettingOpenFlag,SettingTitle.SETTING_GLOBAL)
            if (firstSettingOpenFlag.value) {
                if (scriptSetLocalList.itemCount == 0) {
                    Column(modifier = Modifier.padding(Ui.SPACE_8)) {
                        Text(text = "空空如也，请先去下载脚本！")
                    }
                }else{
                    LazyColumn {
                        items(scriptSetLocalList.itemCount) { index ->
                            scriptSetLocalList[index]?.let { setting ->
                                when (setting.set_type) {
                                    SettingType.SWITCH -> SwitchSettingItem(setting,onSwitchChange = {  settingViewModel.updateGlobalSetting(setting)  })
                                    SettingType.SLIDER -> SliderSettingItem(setting,onSliderValueChange = {  settingViewModel.updateGlobalSetting(setting)  })
                                    SettingType.TEXT_FIELD -> TextFieldSettingItem(setting,onValueChange = {  settingViewModel.updateGlobalSetting(setting)  })
                                    SettingType.CHECK_BOX -> CheckBoxSettingItem(setting,onCheckedChange = {  settingViewModel.updateGlobalSetting(setting)  })
                                    SettingType.RADIO_BUTTON -> RadioButtonSettingItem(setting, onCheckedChange = {  settingViewModel.updateGlobalSetting(setting)  })
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(Ui.SPACE_8))
            //权限设置模块
            CardCustom(secondSettingOpenFlag, SettingTitle.SETTING_PERMISSION)
            val accessibilityServiceOpenFlag = remember {
                mutableStateOf(
                    isAccessibilityServiceEnabled(settingViewModel.context)
                )
            }
            val sharedPreferences = remember {
                settingViewModel.context.getSharedPreferences("permission_setting", Context.MODE_PRIVATE)
            }
            val ignoreBatteryFlag = remember { mutableStateOf(sharedPreferences.getBoolean("ignore_battery", false)) }
            val floatWindowFlag = remember { mutableStateOf(sharedPreferences.getBoolean("float_window", false)) }
            if (secondSettingOpenFlag.value) {
                RowPermissinon(
                    labelText =PermissionSettingText.ACCESSBILITY_SERVICE_TEXT,
                    isSwitchOpen = accessibilityServiceOpenFlag,
                    onSwitchChange = {
                        startActivity(
                            settingViewModel.context,
                            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                            null
                        )
                        accessibilityServiceOpenFlag.value = accessibilityServiceOpenFlag.value
                    /*ServiceCompat.startForeground(
                        AccessibilityService(),
                        ForegroundServiceId.ACCESSIBILITY_SERVICE_ID,
                        NotificationCompat.Builder(
                            settingViewModel.context,
                            Notification()
                        ).setContentTitle("Accessibility Service").build(),
                        ServiceInfo.    //API<29则service.startForeground(id, notification)
                    )*/
                })
                RowPermissinon(PermissionSettingText.SCREEN_RECORD_TEXT,
                    isSwitchOpen = mediaProjectionServiceStartFlag,
                    onSwitchChange={
                   /* ServiceCompat.startForeground(
                        AccessibilityService(),
                        ForegroundServiceId.MEDIA_PROJECTION,
                        NotificationCompat.Builder(
                            settingViewModel.context,
                            Notification()
                        ).setContentTitle("Accessibility Service").build(),
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION    //API<29则service.startForeground(id, notification)
                    )*/
                })
                RowPermissinon(PermissionSettingText.IGNORE_BATTERIES_TEXT,
                    isSwitchOpen = ignoreBatteryFlag,
                    onSwitchChange = {
                        if(it){
                            startActivity(
                                settingViewModel.context,
                                Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                                null)
                            sharedPreferences.edit().putBoolean("ignore_battery", it).apply()
                        }
                        ignoreBatteryFlag.value = it
                    })
                RowPermissinon(PermissionSettingText.FLOAT_WINDOW_TEXT,
                    isSwitchOpen = floatWindowFlag,
                    onSwitchChange = {
                        if(it){
                            startActivity(
                                settingViewModel.context,
                                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                                null)
                            sharedPreferences.edit().putBoolean("float_window", it).apply()
                        }
                        floatWindowFlag.value = it
                    }
                )
            }
        }
    }
}


@Composable
fun CardCustom(
    isExpanded: MutableState<Boolean>,
    labelText: String
) {
    Card(
        onClick = {
            isExpanded.value = !isExpanded.value
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
            }) {
                Icon(imageVector = (if (isExpanded.value) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowLeft)
                    , contentDescription = null)
            }
        }
    }
}

@Composable
fun RowPermissinon(
    labelText: String,
    isSwitchOpen: MutableState<Boolean>,
    onSwitchChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.padding(horizontal = Ui.SPACE_8,vertical = Ui.SPACE_4),
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
    Spacer(modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, MaterialTheme.colorScheme.onBackground))
}


@SuppressLint("ServiceCast")
fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    val accessibilityServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
    for (service in accessibilityServices){
        println("serviceId: ${service.id}")
        println("packageNames: ${service.packageNames}")
        println("resolveInfoName: ${service.resolveInfo.serviceInfo.name}")
        println("resolveInfoPackageName: ${service.resolveInfo.serviceInfo.packageName}")
    }
    return accessibilityServices.any {
        it.id == AccessibilityService::class.java.name
    }
}