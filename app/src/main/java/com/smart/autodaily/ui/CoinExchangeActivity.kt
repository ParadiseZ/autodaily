package com.smart.autodaily.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.paging.compose.collectAsLazyPagingItems
import com.smart.autodaily.constant.AppBarTitle
import com.smart.autodaily.constant.Ui
import com.smart.autodaily.data.entity.KeyTypeExchange
import com.smart.autodaily.ui.conponent.MyButton
import com.smart.autodaily.ui.theme.AutoDailyTheme
import com.smart.autodaily.utils.ScreenCaptureUtil
import com.smart.autodaily.utils.SnackbarUtil
import com.smart.autodaily.utils.gotoCoinDetail
import com.smart.autodaily.viewmodel.person.CoinExchangeViewModel
import kotlinx.coroutines.launch
import splitties.init.appCtx

class CoinExchangeActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AutoDailyTheme {
                Scaffold (
                    topBar = {
                        TopAppBar(title = { Text(text = AppBarTitle.COIN_EXCHANGE) },
                            navigationIcon = {
                                IconButton(onClick ={
                                    onBackPressedDispatcher.onBackPressed()
                                }
                                ){
                                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "null")
                                }
                            }
                        )
                    },
                    snackbarHost = {
                        SnackbarUtil.CustomSnackbarHost()
                    }

                ){
                    CoinExchange(modifier = Modifier.padding(it))
                }
            }
        }
    }
}

@Composable
fun CoinExchange(
    modifier: Modifier,
    viewModel : CoinExchangeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
){
    val user by viewModel.appViewModel.user.collectAsState()
    val keyTypeList = viewModel.keyTypeList.collectAsLazyPagingItems()
    val selectTypeId = remember {
        mutableIntStateOf(-1)
    }
    LaunchedEffect(key1 = true) {
        viewModel.getKeyTypeList()
    }
    val displayMetrics = ScreenCaptureUtil.getDisplayMetrics(appCtx)
    val cols = if(displayMetrics.widthPixels < displayMetrics.heightPixels){2}else{3}
    val exchangeDialog = remember {
        mutableStateOf(false)
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(cols),
        modifier = modifier
        ) {
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = (-2).dp)
            ){
                Text(text = "剩余：" + (user?.virtualCoin?.toString() ?: "0"),fontSize = TextUnit(24f, TextUnitType.Sp))
                MyButton(text ="详情", onclick = {gotoCoinDetail(viewModel.context)})
            }
        }
        items(keyTypeList.itemCount){ keyIdx->
            keyTypeList[keyIdx]?.let {
                KeyTypeItem(it) {
                    exchangeDialog.value = true
                    selectTypeId.intValue = it.id
                }
            }
        }
    }

    if(exchangeDialog.value){
        AlertDialog(
            onDismissRequest = {
                exchangeDialog.value = false
            },
            confirmButton = {
                OutlinedButton(
                    enabled = exchangeDialog.value,
                    onClick = {
                        viewModel.viewModelScope.launch {
                            val res = viewModel.exchangeVip(selectTypeId.intValue)
                            res?.let {
                                it.message?.let { msg->
                                    SnackbarUtil.show(msg)
                                }
                            }
                            exchangeDialog.value = false
                        }
                    }
                ){
                    Text(text = "确定")
                }
            },
            dismissButton = {
                OutlinedButton(
                    enabled = exchangeDialog.value,
                    onClick = {
                        exchangeDialog.value = false
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
                    text = "您确定要兑换它吗？",
                    fontSize = Ui.SIZE_16
                )
            },
        )
    }
}

@Composable
fun KeyTypeItem(
    keyType: KeyTypeExchange, onClick: () -> Unit
){
    Column (
        modifier = Modifier.padding(bottom = 20.dp)
    ){
        Text(text = keyType.typeName+"赞助"+keyType.vipLevel,fontSize = TextUnit(16f, TextUnitType.Sp))
        Text(text = "单次额外运行："+keyType.canRunNum,fontSize = TextUnit(12f, TextUnitType.Sp))
        Row {
            Column {
                Text(text = keyType.buyPrice.toString()+"AD币",fontSize = TextUnit(12f, TextUnitType.Sp))
                Text(text = keyType.price.toString()+"AD币",textDecoration =TextDecoration.LineThrough,fontSize = TextUnit(12f, TextUnitType.Sp))
            }
            MyButton(text ="兑换", onclick = {onClick()})
        }

    }
}