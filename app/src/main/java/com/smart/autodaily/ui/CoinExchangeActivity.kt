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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.paging.compose.collectAsLazyPagingItems
import com.smart.autodaily.constant.AppBarTitle
import com.smart.autodaily.data.entity.KeyTypeExchange
import com.smart.autodaily.ui.theme.AutoDailyTheme
import com.smart.autodaily.utils.ScreenCaptureUtil
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
    LazyVerticalGrid(
        columns = GridCells.Fixed(cols),
        modifier = modifier
        ) {
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Text(text = "剩余：" + (user?.virtualCoin?.toString() ?: "0"),fontSize = TextUnit(24f, TextUnitType.Sp))
                Button(onClick = {
                    //appCtx.startActivity()
                }) {
                    Text(text = "详情")
                }
            }
        }
        items(keyTypeList.itemCount){ keyIdx->
            keyTypeList[keyIdx]?.let {
                KeyTypeItem(it) {
                    selectTypeId.intValue = it.id
                }
            }
        }
    }
    LaunchedEffect(key1 = selectTypeId.intValue){
        if (selectTypeId.intValue != -1){
            viewModel.viewModelScope.launch {
                viewModel.exchangeVip(selectTypeId.intValue)
            }
        }
    }
}

@Composable
fun KeyTypeItem(
    keyType: KeyTypeExchange, onClick: () -> Unit
){
    Column (
        modifier = Modifier.padding(bottom = 20.dp)
    ){
        Text(text = keyType.typeName+"vip"+keyType.vipLevel,fontSize = TextUnit(16f, TextUnitType.Sp))
        Text(text = "单次可运行个数："+keyType.canRunNum,fontSize = TextUnit(12f, TextUnitType.Sp))
        Row {
            Column {
                Text(text = keyType.buyPrice.toString()+"AD币",fontSize = TextUnit(12f, TextUnitType.Sp))
                Text(text = keyType.price.toString()+"AD币",textDecoration =TextDecoration.LineThrough,fontSize = TextUnit(12f, TextUnitType.Sp))
            }
            Button(onClick = {
                onClick()
            }) {
                Text(text = "兑换")
            }
        }

    }
}