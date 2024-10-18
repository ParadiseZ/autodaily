package com.smart.autodaily.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.smart.autodaily.constant.SettingType
import com.smart.autodaily.data.entity.ScriptSetInfo
import com.smart.autodaily.viewmodel.home.ScriptSetViewModel

@Composable
fun ScriptSetScreen(
    modifier: Modifier,
    viewModel : ScriptSetViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    selectId : Int
){
    val scripts by viewModel.appViewModel.localScriptListAll.collectAsState()
    val scriptSets by viewModel.scriptSetList.collectAsState()
    val state = remember {
        mutableIntStateOf(selectId)
    }
    LaunchedEffect (key1 = state.intValue){
        viewModel.getScriptSetById(state.intValue)
    }
    if (scripts.isNotEmpty()){
        ScrollableTabRow(
            modifier = modifier,
            selectedTabIndex = scripts.indexOfFirst { it.scriptId==state.intValue }) {
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
            modifier= modifier.padding(top = 36.dp)
        ){
            items(scriptSets){ set->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0..<set.setLevel){
                        Spacer(modifier = Modifier.width(14.dp))
                    }
                    when(set.setType){
                        SettingType.SWITCH -> {}
                        SettingType.SLIDER -> {}
                        SettingType.TEXT_FIELD -> {}
                        SettingType.CHECK_BOX -> SetCheckBox(set, onClick = {viewModel.updateScriptSet(set)})
                        SettingType.RADIO_BUTTON -> {}
                        SettingType.SLIDER_SECOND -> {}
                        SettingType.TITLE -> SetTitle(set)
                        SettingType.SLIDER_THIRD  -> {}
                        SettingType.DROPDOWN_MENU -> SetExposedDropdownMenu(set = set, onClick = {  viewModel.updateScriptSet(set)  })
                    }
                }
            }
        }
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
        onClick()
    })
    Text(modifier = Modifier
        .clickable {
            isChecked = !isChecked
            set.checkedFlag = isChecked
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