package com.learn.test_exel.ui


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.learn.test_exel.Backend.data.QrCodec
import com.learn.test_exel.ui.viewmodel.HospitalViewModel
import com.learn.test_exel.ui.viewmodel.ScanInfo
import kotlinx.coroutines.launch

@Composable
fun QrScanScreen(vm: HospitalViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val options by vm.treatmentOptions.collectAsState()

    var expectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var patientOk by rememberSaveable { mutableStateOf(false) }
    var drugOk by rememberSaveable { mutableStateOf(false) }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    var messageOk by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }

    val expected = options.firstOrNull { it.treatmentId == expectedId }

    fun resetResults() { patientOk = false; drugOk = false; message = null }

    fun onScanned(raw: String?) {
        val exp = expected ?: return
        val payload = QrCodec.parse(raw)
        if (payload == null) {
            message = "الكود ده مش تابع للتطبيق"; messageOk = false
            return
        }
        scope.launch {
            when (val info = vm.resolveScan(payload)) {
                ScanInfo.Missing -> {
                    message = "الكود تابع للتطبيق لكن بياناته مش موجودة على الجهاز. اسحب البيانات من Firebase وجرّب تاني"
                    messageOk = false
                }
                is ScanInfo.P -> {
                    if (info.patient.id == exp.patientId) {
                        patientOk = true; messageOk = true
                        message = "✓ المريض مطابق: ${info.patient.name}"
                    } else {
                        patientOk = false; messageOk = false
                        message = "✗ مريض مختلف: ${info.patient.name} (سرير ${info.patient.bedNumber}). المطلوب: ${exp.patientName} (سرير ${exp.bedNumber})"
                    }
                }
                is ScanInfo.T -> {
                    val o = info.option
                    if (o.treatmentId == exp.treatmentId) {
                        drugOk = true; patientOk = true; messageOk = true
                        message = "✓ العلاج مطابق: ${o.drugName} للمريض ${o.patientName}"
                    } else {
                        drugOk = false; messageOk = false
                        message = if (o.patientId == exp.patientId)
                            "✗ دواء مختلف: ${o.drugName}. المطلوب: ${exp.drugName}"
                        else
                            "✗ ده علاج مريض تاني: ${o.patientName} - ${o.drugName}. المطلوب: ${exp.patientName} - ${exp.drugName}"
                    }
                }
            }
        }
    }

    fun startScan() {
        val opts = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        GmsBarcodeScanning.getClient(context, opts).startScan()
            .addOnSuccessListener { onScanned(it.rawValue) }
            .addOnFailureListener { vm.toast("تعذر فتح الماسح: ${it.message}") }
    }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("قراءة QR ومقارنة", style = MaterialTheme.typography.headlineSmall)

        if (expected == null) {
            Text("اختار العلاج المطلوب الأول:")
            OutlinedTextField(
                query, { query = it }, Modifier.fillMaxWidth(),
                label = { Text("بحث") }, singleLine = true
            )
            val shown = options.filter {
                val q = query.trim()
                q.isEmpty() || it.patientName.contains(q, true) ||
                        it.drugName.contains(q, true) || it.bedNumber.contains(q, true)
            }
            if (shown.isEmpty()) {
                Text("لا توجد بيانات. أضف علاج أو اسحب البيانات من Firebase.")
            } else {
                LazyColumn(Modifier.weight(1f)) {
                    items(shown, key = { it.treatmentId }) { o ->
                        ListItem(
                            headlineContent = { Text("${o.patientName} - ${o.drugName}") },
                            supportingContent = { Text("سرير ${o.bedNumber}") },
                            modifier = Modifier.clickable { expectedId = o.treatmentId; resetResults() }
                        )
                        HorizontalDivider()
                    }
                }
            }
        } else {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("المطلوب:", style = MaterialTheme.typography.labelMedium)
                    Text(expected.patientName, style = MaterialTheme.typography.titleMedium)
                    Text("سرير ${expected.bedNumber}  |  ${expected.drugName}")
                    Spacer(Modifier.height(8.dp))
                    Text(if (patientOk) "✓ المريض: تم التحقق" else "○ المريض: لسه ما اتمسحش")
                    Text(if (drugOk) "✓ الدواء: تم التحقق" else "○ الدواء: لسه ما اتمسحش")
                }
            }

            if (patientOk && drugOk) {
                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Text(
                        "✓ تطابق كامل: المريض والدواء صح",
                        Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            message?.let {
                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (messageOk) MaterialTheme.colorScheme.secondaryContainer
                        else MaterialTheme.colorScheme.errorContainer
                    )
                ) { Text(it, Modifier.padding(16.dp)) }
            }

            Button(onClick = { startScan() }, Modifier.fillMaxWidth()) { Text("مسح QR") }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { resetResults() }, Modifier.weight(1f)) { Text("إعادة ضبط") }
                OutlinedButton(
                    onClick = { expectedId = null; resetResults() }, Modifier.weight(1f)
                ) { Text("تغيير الاختيار") }
            }
        }
    }
}