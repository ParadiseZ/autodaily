package com.smart.autodaily.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
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
                        TopAppBar(title = { Text(text = AppBarTitle.COIN_EXCHANGE) })
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
    Row (modifier = modifier.fillMaxWidth()){
        user?.virtualCoin.let {
            Text(text =it.toString())
        }
        Button(onClick = {

            //appCtx.startActivity()
        }) {
            Text(text = "详情")
        }
    }
    val displayMetrics = ScreenCaptureUtil.getDisplayMetrics(appCtx)
    val cols = if(displayMetrics.widthPixels < displayMetrics.heightPixels){2}else{3}
    LazyVerticalGrid(columns = GridCells.Fixed(cols), modifier = modifier) {
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
fun KeyTypeItem(keyType: KeyTypeExchange, onClick: () -> Unit){
    Box {
        Text(text = keyType.typeName+"vip"+keyType.vipLevel)
        Text(text = keyType.buyPrice.toString()+"AD币")
        Text(text = keyType.price.toString()+"AD币",textDecoration =TextDecoration.LineThrough)
        Button(onClick = {
            onClick()
        }) {
            Text(text = "兑换")
        }
    }
}