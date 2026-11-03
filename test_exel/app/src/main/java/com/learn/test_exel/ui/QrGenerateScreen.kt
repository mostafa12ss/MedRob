package com.learn.test_exel.ui


import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.learn.test_exel.Backend.data.QrCodec
import com.learn.test_exel.Backend.data.QrKind
import com.learn.test_exel.ui.viewmodel.HospitalViewModel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private data class Entry(val id: String, val title: String, val subtitle: String)

@Composable
fun QrGenerateScreen(vm: HospitalViewModel) {
    val patients by vm.patients.collectAsState()
    val options by vm.treatmentOptions.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var kind by rememberSaveable { mutableStateOf(QrKind.PATIENT) }
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var query by rememberSaveable { mutableStateOf("") }

    val all = remember(kind, patients, options) {
        if (kind == QrKind.PATIENT)
            patients.map { Entry(it.id, it.name, "سرير ${it.bedNumber}") }
        else
            options.map { Entry(it.treatmentId, "${it.patientName} - ${it.drugName}", "سرير ${it.bedNumber}") }
    }
    val shown = remember(all, query) {
        val q = query.trim()
        if (q.isEmpty()) all else all.filter { it.title.contains(q, true) || it.subtitle.contains(q, true) }
    }
    val selected = all.firstOrNull { it.id == selectedId }

    val bitmap: Bitmap? = remember(kind, selected?.id) {
        selected?.let { QrCodec.bitmap(QrCodec.encode(kind, it.id)) }
    }

    val saver = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/png")) { uri ->
        val bmp = bitmap
        if (uri != null && bmp != null) {
            scope.launch(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openOutputStream(uri)?.use {
                        bmp.compress(Bitmap.CompressFormat.PNG, 100, it)
                    }
                }.onSuccess { vm.toast("تم حفظ صورة الكود") }
                    .onFailure { vm.toast("فشل حفظ الصورة") }
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("إنشاء QR Code", style = MaterialTheme.typography.headlineSmall)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = kind == QrKind.PATIENT,
                onClick = { kind = QrKind.PATIENT; selectedId = null },
                label = { Text("مريض") }
            )
            FilterChip(
                selected = kind == QrKind.TREATMENT,
                onClick = { kind = QrKind.TREATMENT; selectedId = null },
                label = { Text("علاج (مريض + دواء)") }
            )
        }

        if (selected != null && bitmap != null) {
            Card(Modifier.fillMaxWidth()) {
                Column(
                    Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // خلفية بيضاء ثابتة عشان الكود يتقرأ حتى في الوضع الداكن
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier.size(220.dp)
                    )
                    Text(selected.title, style = MaterialTheme.typography.titleMedium)
                    Text(selected.subtitle, style = MaterialTheme.typography.bodySmall)
                    OutlinedButton(onClick = {
                        saver.launch("qr_${kind.code}_${selected.id.take(8)}.png")
                    }) { Text("حفظ كصورة") }
                }
            }
        }

        OutlinedTextField(
            query, { query = it }, Modifier.fillMaxWidth(),
            label = { Text("بحث بالاسم أو السرير أو الدواء") }, singleLine = true
        )

        if (shown.isEmpty()) {
            Text("لا توجد بيانات. أضف علاج أو اسحب البيانات من Firebase.")
        } else {
            LazyColumn(Modifier.weight(1f)) {
                items(shown, key = { it.id }) { e ->
                    ListItem(
                        headlineContent = { Text(e.title) },
                        supportingContent = { Text(e.subtitle) },
                        colors = ListItemDefaults.colors(
                            containerColor = if (e.id == selectedId)
                                MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
                        ),
                        modifier = Modifier.clickable { selectedId = e.id }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}