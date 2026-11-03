package com.learn.test_exel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.learn.test_exel.ui.DataScreen
import com.learn.test_exel.ui.QrGenerateScreen
import com.learn.test_exel.ui.QrScanScreen

import com.learn.test_exel.ui.UploadScreen
import com.learn.test_exel.ui.theme.Test_exelTheme
import com.learn.test_exel.ui.viewmodel.HospitalViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { Test_exelTheme { AppRoot() } }
    }
}

@Composable
fun AppRoot(vm: HospitalViewModel = viewModel()) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { vm.messages.collect { snackbar.showSnackbar(it) } }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == 0, onClick = { tab = 0 },
                    icon = { Icon(painter = painterResource(R.drawable.baseline_add_24), null) }, label = { Text("رفع") }
                )
                NavigationBarItem(
                    selected = tab == 1, onClick = { tab = 1 },
                    icon = { Icon(painter = painterResource(R.drawable.baseline_add_24),null) }, label = { Text("عرض") }
                )
                NavigationBarItem(
                    selected = tab == 2, onClick = { tab = 2 },
                    icon = { Icon(painter = painterResource(R.drawable.baseline_folder_shared_24), null) }, label = { Text("إنشاء QR") }
                )
                NavigationBarItem(
                    selected = tab == 3, onClick = { tab = 3 },
                    icon = { Icon(painter = painterResource(R.drawable.baseline_content_paste_search_24), null) }, label = { Text("قراءة QR") }
                )
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (tab) {
                0 -> UploadScreen(vm)
                1 -> DataScreen(vm)
                2 -> QrGenerateScreen(vm)
                else -> QrScanScreen(vm)
            }
        }
    }
}