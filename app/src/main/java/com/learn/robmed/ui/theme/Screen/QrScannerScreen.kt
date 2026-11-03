package com.learn.robmed.ui.theme.Screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.learn.robmed.tools.ScanResult
import com.learn.robmed.tools.parseQr
import com.learn.robmed.ui.theme.support.CameraAnalysisView
import java.util.concurrent.atomic.AtomicBoolean

/**
 * scanKey: غيّره (scanKey++) لو عايز السكانر يقرا تاني بعد قراءة سابقة.
 */
@Composable
fun QrScannerScreen(scanKey: Int = 0, onResult: (ScanResult) -> Unit) {
    val context = LocalContext.current
    val currentOnResult by rememberUpdatedState(onResult)

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { hasPermission = it }

    LaunchedEffect(Unit) {
        if (!hasPermission) launcher.launch(Manifest.permission.CAMERA)
    }

    if (!hasPermission) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("محتاجين إذن الكاميرا عشان نقرا الـ QR")
        }
        return
    }

    val handled = remember { AtomicBoolean(false) }
    LaunchedEffect(scanKey) { handled.set(false) }

    val scanner = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
        )
    }

    Box(Modifier.fillMaxSize()) {
        CameraAnalysisView(
            modifier = Modifier.fillMaxSize(),
            onRelease = { scanner.close() }
        ) { image, finish ->
            if (handled.get()) {
                finish()
                return@CameraAnalysisView
            }
            scanner.process(image)
                .addOnSuccessListener { codes ->
                    val raw = codes.firstOrNull()?.rawValue
                    if (raw != null && handled.compareAndSet(false, true)) {
                        currentOnResult(parseQr(raw))
                    }
                }
                .addOnCompleteListener { finish() }
        }

        // إطار توضيحي للمسح
        Box(
            Modifier
                .align(Alignment.Center)
                .size(240.dp)
                .border(3.dp, Color.White, RoundedCornerShape(16.dp))
        )
    }
}