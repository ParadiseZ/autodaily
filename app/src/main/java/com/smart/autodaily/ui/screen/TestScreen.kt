package com.smart.autodaily.ui.screen

import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.smart.autodaily.viewmodel.SearchViewModel

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.smart.autodaily.R
import com.smart.autodaily.constant.Ui
import com.smart.autodaily.data.entity.ScriptInfo

@Composable
fun TestScreen(
    searchViewModel: SearchViewModel = viewModel()
)
{
    val netSearScriptList = searchViewModel.searchScriptByPage().collectAsLazyPagingItems()
    LazyColumn {
        this.items(
            netSearScriptList.itemCount
        ){index ->
            netSearScriptList[index]?.let { TestRow(it) }
        }
       /* this.itemsIndexed(netSearScriptList.itemSnapshotList){  index, scriptInfo ->
            if (scriptInfo != null) {
                TestRow(scriptInfo)
            }
        }*/
    }
}

@Composable
fun TestRow(
    scriptInfo: ScriptInfo,
) {
    Card(
        modifier = Modifier
            .clickable {
                //onItemClicked(item)
            },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Ui.SPACE_5),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Ui.SPACE_5),
                //modifier = Modifier.padding(vertical = Ui.SPACE_5)
            ) {
                Spacer(modifier = Modifier.width(Ui.SPACE_5))
                //多选框
                /*                var checkboxState by remember {
                                    mutableStateOf( scriptInfo.isChecked )
                                }*/
                //图标信息
                if (scriptInfo != null) {
                    Text(text = scriptInfo.script_id.toString())
                }
                Image(
                    painter = painterResource(id = R.drawable.bh3_offi),
                    contentDescription = null,
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(20))
                        .size(Ui.IMG_SIZE_50)
                )
                //脚本名称信息等
                Column() {
                    if (scriptInfo != null) {
                        androidx.compose.material3.Text(
                            text = scriptInfo.script_name,
                            fontSize = Ui.SIZE_16
                        )
                    }
                    if (scriptInfo != null) {
                        androidx.compose.material3.Text(
                            text = scriptInfo.script_version,
                            fontSize = Ui.SIZE_10
                        )
                    }
                }
            }
            //操作按钮
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Ui.SPACE_5),
            ) {
                IconButton(
                    modifier = Modifier
                        .border(
                            width = Ui.SPACE_1,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(50)
                        )
                        .size(Ui.ICON_SIZE_40),
                    content = {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = null
                        )
                    },
                    onClick = {}
                )
            }
        }
    }
}