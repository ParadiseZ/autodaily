package com.smart.autodaily.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.paging.compose.collectAsLazyPagingItems
import com.smart.autodaily.constant.AppBarTitle
import com.smart.autodaily.ui.conponent.SwipeRefreshList
import com.smart.autodaily.ui.theme.AutoDailyTheme
import com.smart.autodaily.viewmodel.person.CoinRecordViewModel

class CoinDetailActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AutoDailyTheme {
                Scaffold (
                    topBar = {
                        TopAppBar(title = { Text(text = AppBarTitle.COIN_DETAIL_SCREEN) })
                    }
                ){
                    CoinDetailScreen(modifier = Modifier.padding(it))
                }
            }
        }
    }
}
@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun CoinDetailScreen(
    modifier: Modifier,
    viewModel:CoinRecordViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
){

    val virtualCoinRecords = viewModel.virtualcoinRecordList.collectAsLazyPagingItems()
    LaunchedEffect(key1 = true) {
        viewModel.getVirtualCoinRecord()
    }
    SwipeRefreshList(
        collectAsLazyPagingItems = virtualCoinRecords,
        modifier = modifier,
        listContent ={ virtualCoin ->
            Row (
                modifier = modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Column {
                    if ("+" == virtualCoin.changeType){
                        Text(text = "返利")
                    }else if ("-" == virtualCoin.changeType){
                        Text(text = "兑换")
                    }
                    Text(text = "日期")
                }
                Text(text = virtualCoin.changeValue)
            }
        }
    )
}