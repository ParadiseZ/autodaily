package com.smart.autodaily.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.smart.autodaily.constant.AppBarTitle
import com.smart.autodaily.constant.SettingType
import com.smart.autodaily.data.entity.ScriptSetInfo
import com.smart.autodaily.utils.SnackbarUtil
import com.smart.autodaily.viewmodel.home.ScriptSetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScriptSetScreen(
    modifier: Modifier,
    viewModel : ScriptSetViewModel = viewModel(),
    nhc: NavHostController,
    selectId : Int?
){
    val scripts by viewModel.appViewModel.localScriptListAll.collectAsState()
    val scriptSets by viewModel.scriptSetList.collectAsState()
    val state = remember {
        mutableIntStateOf(selectId?:1)
    }
    LaunchedEffect (key1 = state.intValue){
        viewModel.getScriptSetById(state.intValue)
    }
    Scaffold (
        topBar = {
            TopAppBar(
                title = {
                    Text(text = AppBarTitle.SCRIPT_SET_DETAIL)
                },
                navigationIcon = {
                    IconButton(onClick ={
                        nhc.popBackStack()
                    }
                    ){
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarUtil.CustomSnackbarHost()
        }
    ) { padding->
        if (scripts.isNotEmpty()) {
            ScrollableTabRow(
                modifier = Modifier.padding(padding),
                selectedTabIndex = scripts.indexOfFirst { it.scriptId == state.intValue }) {
                scripts.forEach {
                    Tab(
                        modifier = Modifier.padding(5.dp),
                        selected = it.scriptId == state.intValue,
                        onClick = {
                            state.intValue = it.scriptId
                        }
                    ) {
                        Text(text = it.scriptName)
                    }
                }

            }
            LazyColumn(
                modifier = modifier.padding(padding).padding(top = 14.dp)
            ) {
                items(scriptSets) { set ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0..<set.setLevel) {
                            Spacer(modifier = Modifier.width(20.dp))
                        }
                        when (set.setType) {
                            SettingType.SWITCH -> {}
                            SettingType.SLIDER_SIXTH -> {
                                SliderItem(set,
                                    onCheckBoxValueChange = {
                                        viewModel.updateScriptSet(set)
                                    },
                                    onSliderValueChange ={
                                        viewModel.updateScriptSet(set)
                                        viewModel.updateActionExecuteMax(set)
                                    }
                                )
                            }
                            SettingType.TEXT_FIELD -> {}
                            SettingType.CHECK_BOX -> SetCheckBox(
                                set,
                                onClick = { viewModel.updateScriptSet(set) })

                            SettingType.RADIO_BUTTON -> RadioButtonThis(Modifier,set) {
                                viewModel.updateScriptSet(set)
                            }
                            SettingType.TITLE -> SetTitle(set)
                            SettingType.DROPDOWN_MENU -> SetExposedDropdownMenu(
                                set = set,
                                onClick = { viewModel.updateScriptSet(set) })
                            else -> {

                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun SliderItem(
    setting: ScriptSetInfo,
    onSliderValueChange: (ScriptSetInfo) -> Unit,
    onCheckBoxValueChange :(ScriptSetInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    var checked by remember { mutableStateOf(setting.checkedFlag) }
    var sliderValue by remember { mutableFloatStateOf(
        if (setting.setValue?.isNotBlank() == true) {
          try {
              setting.setValue!!.toFloat()
          }catch (_: Exception){
             setting.setDefaultValue?.toFloat() ?: 0f
          }
        } else setting.setDefaultValue?.toFloat() ?: 0f
    ) }
    Row(
        //modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = {
                checked = it
                setting.checkedFlag = it
                onCheckBoxValueChange(setting)
            },
        )
        val setRange = setting.setRange?.split(',')!!
        Text(text = setting.setName)
        when(setting.setType){
            SettingType.SLIDER_SIXTH -> Text(text = "${"%.0f".format(sliderValue )}次")
            SettingType.SLIDER_SECOND -> Text(text = "${"%.1f".format(sliderValue )}秒")
            SettingType.SLIDER_THIRD -> Text(text = "${"%.0f".format(sliderValue )}px")
            SettingType.SLIDER_FOURTH -> Text(text = "${"%.1f".format(sliderValue )}分")
            SettingType.SLIDER_FIFTH -> Text(text = "${"%.0f".format(sliderValue )}倍")
            else -> Text(text = "%2.f".format(sliderValue))
        }
        Slider(
            modifier = Modifier.padding(end = 20.dp),
            valueRange = setRange[0].toFloat()..setRange[1].toFloat(),
            steps = setting.setStep,
            value = sliderValue,
            colors = SliderDefaults.colors(
                thumbColor = Color.White, // 圆圈的颜色
                activeTrackColor = Color(0xFF0079D3)
            ),
            onValueChange = {
                sliderValue = it
                when(setting.setType){
                    SettingType.SLIDER_SIXTH -> setting.setValue = "%.0f".format(it)
                    SettingType.SLIDER_FIFTH -> setting.setValue = "%.0f".format(it)
                    else->setting.setValue = it.toString()
                }
                onSliderValueChange(setting)
            })

    }
}

@Composable
fun SetCheckBox(set: ScriptSetInfo, onClick : ()->Unit){
    var isChecked by remember {
        mutableStateOf(
            set.checkedFlag
        )
    }
    Checkbox(checked = isChecked, onCheckedChange = {
        isChecked = it
        set.checkedFlag = it
        set.setValue = if(isChecked) "true" else "false"
        onClick()
    })
    Text(modifier = Modifier
        .clickable {
            isChecked = !isChecked
            set.checkedFlag = isChecked
            set.setValue = if(isChecked) "true" else "false"
            onClick()
        },
        text = set.setName)
}

@Composable
fun SetTitle(set: ScriptSetInfo){
    Text(text = "★"+set.setName)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetExposedDropdownMenu(set: ScriptSetInfo, onClick : ()->Unit){
    val options: List<String> = set.setDefaultValue!!.split(",")
    var expanded by remember { mutableStateOf(false) }
    val textFieldState = remember{
        mutableStateOf(options[options.indexOf(set.setValue)])
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        TextField(
            value = textFieldState.value,
            onValueChange = {
                textFieldState.value = it
            },
            label = {
                Text(text = set.setName)
            },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
            readOnly = true,
            singleLine = true,
            trailingIcon = {
                           Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, style = MaterialTheme.typography.bodyLarge) },
                    onClick = {
                        textFieldState.value = option
                        expanded = false
                        set.setValue = option
                        onClick()
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

@Composable
fun RadioButtonThis(
    modifier: Modifier,
    set: ScriptSetInfo, onClick : (set:ScriptSetInfo)->Unit,
){
    var setValue by remember {
        mutableStateOf(set.setValue)
    }
    Text(text = set.setName)
    set.setDefaultValue?.split(",")?.forEach {
        RadioButton(
            selected = it == setValue,
            onClick = {
                setValue = it
                set.setValue = it
                onClick(set)
            }
        )
        Text(modifier = Modifier.clickable{
            setValue = it
            set.setValue = it
            onClick(set)
        },text = it)
    }
}