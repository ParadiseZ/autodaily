package com.smart.autodaily.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.paging.compose.collectAsLazyPagingItems
import com.smart.autodaily.constant.AppBarTitle
import com.smart.autodaily.data.entity.UserKeyRecord
import com.smart.autodaily.ui.theme.AutoDailyTheme
import com.smart.autodaily.viewmodel.person.UserKeyRecordViewModel

class UserKeyRecordActivity: ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AutoDailyTheme {
                Scaffold (
                    topBar = {
                        TopAppBar(title = { Text(text = AppBarTitle.USER_KEY_RECORD) },
                            navigationIcon = {
                                IconButton(onClick ={
                                    onBackPressedDispatcher.onBackPressed()
                                }
                                ){
                                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "null")
                                }
                            }
                        )
                    }
                ){
                    UserKeyRecordScreen(modifier = Modifier.padding(it))
                }
            }
        }
    }
}

@Composable
fun UserKeyRecordScreen(
    modifier: Modifier,
    viewModel: UserKeyRecordViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
){
    val userKeyRecords = viewModel.userKeyRecords.collectAsLazyPagingItems()
    LaunchedEffect(key1 = true) {
        viewModel.getUserKeyRecord()
    }
    if (userKeyRecords.itemCount == 0){
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
            Text(text = "嗯......空空如也")
        }
    }else{
        LazyVerticalGrid(columns = GridCells.Fixed(1), modifier = modifier) {
            items(userKeyRecords.itemCount){ keyIdx->
                userKeyRecords[keyIdx]?.let {
                    UserKeyRecordItem(it)
                }
            }
        }
    }

}

@Composable
fun UserKeyRecordItem(uk: UserKeyRecord){
    Text(text = uk.vipLevel)
    Text(text = uk.addTime+"~"+uk.expirationTime)
    Text(text = uk.createDesc)
}