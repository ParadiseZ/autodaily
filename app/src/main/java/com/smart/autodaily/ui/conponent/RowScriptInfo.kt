package com.smart.autodaily.ui.conponent

import android.annotation.SuppressLint
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.smart.autodaily.constant.BLUE_01
import com.smart.autodaily.constant.Ui
import com.smart.autodaily.data.entity.ScriptInfo

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun RowScriptInfo(
    modifier: Modifier = Modifier,
    cardOnClick: (scriptInfo: ScriptInfo) -> Unit,
    surface: @Composable  () -> Unit = {},
    checkBox: @Composable  () -> Unit = {},
    iconInfo: @Composable  () -> Unit = {},
    scriptInfo : ScriptInfo,
    processShow : MutableState<Boolean> = mutableStateOf(false),
    downloaded : MutableIntState = mutableIntStateOf(0)
) {
    val process by remember{
        scriptInfo.process
    }
    Spacer(modifier = Modifier.height(8.dp))
    Card(
        modifier = modifier
            .clickable {
                cardOnClick(scriptInfo)
            }.drawWithContent  {
                drawContent()
                if (scriptInfo.isDownloaded== 1 && scriptInfo.lastVersion!=null){
                    drawCircle(Color(0xffe7614e), 18.dp.toPx() / 2, center = Offset(drawContext.size.width, 0f))
                }
            }
    ){
        Row (
            modifier = modifier
                .fillMaxWidth()
                .padding(top = Ui.SPACE_5),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Row (
                modifier = modifier.weight(3f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy( Ui.SPACE_5   ),
            ){
                Spacer(modifier = Modifier.width( Ui.SPACE_5 ))
                surface()
                checkBox()
                //脚本名称信息等
                Column{
                    Text(text = scriptInfo.scriptName, fontSize = Ui.SIZE_16 )
                    Spacer(modifier = Modifier.height( Ui.SPACE_5 ))
                    Row{
                        if(scriptInfo.isDownloaded == 1){
                            Text(text = "版本："+scriptInfo.scriptVersion, fontSize = Ui.SIZE_10 )
                            Spacer(modifier = Modifier.width( Ui.SPACE_5 ))
                            if ( scriptInfo.lastVersion!=null && scriptInfo.scriptVersion != scriptInfo.lastVersion){
                                Text(text = "最新："+scriptInfo.lastVersion, fontSize = Ui.SIZE_10 )
                            }
                        }else{
                            Text(text = "版本："+scriptInfo.lastVersion, fontSize = Ui.SIZE_10 )
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
        Spacer(modifier = modifier.height(Ui.SPACE_5))
        if (processShow.value && process>0 && process!=100){
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Ui.SPACE_5),
                horizontalArrangement = Arrangement.Center
            ){
                LinearProgressIndicator(
                    progress = { process.toFloat()/100 },
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(BLUE_01),
                )
            }
        }
        if ( process == -1 ){
            downloaded.intValue = 1
        }
    }
}
