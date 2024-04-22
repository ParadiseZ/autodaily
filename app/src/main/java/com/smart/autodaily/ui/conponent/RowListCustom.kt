package com.smart.autodaily.ui.conponent

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.smart.autodaily.constant.Ui
import com.smart.autodaily.data.entity.ScriptInfo

@Composable
fun RowListCustom(
    modifier: Modifier = Modifier,
    cardOnClick: (scriptInfo: ScriptInfo) -> Unit,
    checkBox: @Composable  () -> Unit = {},
    iconInfo: @Composable  () -> Unit = {},
    scriptInfo : ScriptInfo
) {
    Card(
        modifier = modifier
            .clickable {
                //onItemClicked(item)
                cardOnClick(scriptInfo)
            }
    ){
        Row (
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = Ui.SPACE_10),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Row (
                modifier = modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy( Ui.SPACE_5   ),
            ){
                Spacer(modifier = Modifier.width( Ui.SPACE_5 ))
                checkBox()
                //脚本名称信息等
                Column{
                    Text(text = scriptInfo.script_name, fontSize = Ui.SIZE_16 )
                    Spacer(modifier = Modifier.height( Ui.SPACE_5 ))
                    Row{
                        if(scriptInfo.is_downloaded == 1){
                            Text(text = "版本："+scriptInfo.script_version, fontSize = Ui.SIZE_10 )
                            Spacer(modifier = Modifier.width( Ui.SPACE_5 ))
                            Text(text = "最新："+scriptInfo.last_version, fontSize = Ui.SIZE_10 )
                        }else{
                            Text(text = "当前："+scriptInfo.script_version, fontSize = Ui.SIZE_10 )
                        }
                    }
                }
            }
            Row (
                modifier = modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ){
                iconInfo()
            }
            Spacer(modifier = Modifier.width( Ui.SPACE_5 ))
        }
    }
}
