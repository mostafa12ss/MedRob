package com.learn.test_exel.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.learn.test_exel.ui.viewmodel.HospitalViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

private fun parseTimes(text: String): List<LocalTime>? = runCatching {
    text.split(',', '،', ';', ' ')
        .map { it.trim() }.filter { it.isNotEmpty() }
        .map { s ->
            val parts = s.split(":")
            LocalTime.of(parts[0].toInt(), parts.getOrNull(1)?.toInt() ?: 0)
        }.distinct()
}.getOrNull()?.takeIf { it.isNotEmpty() }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(vm: HospitalViewModel) {
    var patient by rememberSaveable { mutableStateOf("") }
    var bed by rememberSaveable { mutableStateOf("") }
    var drug by rememberSaveable { mutableStateOf("") }
    var startDay by rememberSaveable { mutableStateOf<Long?>(null) }
    var endDay by rememberSaveable { mutableStateOf<Long?>(null) }
    var timesText by rememberSaveable { mutableStateOf("08:00, 20:00") }
    var pickFor by remember { mutableStateOf<Int?>(null) }   // 1=بداية، 2=نهاية
    val busy by vm.busy.collectAsState()
    val preview by vm.preview.collectAsState()
    val xlsxMime = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"

    val pickExcel = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { vm.readExcel(it) }
    }
    val saveTemplate = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(xlsxMime)) { uri ->
        uri?.let { vm.saveTemplate(it) }
    }

    preview?.let { p ->
        AlertDialog(
            onDismissRequest = vm::cancelImport,
            title = { Text("معاينة الاستيراد") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text("صفوف سليمة: ${p.rows.size}   |   صفوف فيها أخطاء: ${p.errors.size}")
                    Spacer(Modifier.height(8.dp))
                    p.rows.take(5).forEach { r ->
                        Text(
                            "• ${r.patient} (سرير ${r.bed}) - ${r.drug} | أخد: ${r.takenCount} - ما أخدش: ${r.missedCount} - لسه: ${r.pendingCount}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (p.rows.size > 5) {
                        Text("... و${p.rows.size - 5} صف آخر", style = MaterialTheme.typography.bodySmall)
                    }
                    if (p.errors.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text("الصفوف دي مش هتتحفظ:", color = MaterialTheme.colorScheme.error)
                        p.errors.take(8).forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
                        if (p.errors.size > 8) {
                            Text("... و${p.errors.size - 8} خطأ آخر", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = vm::confirmImport, enabled = p.rows.isNotEmpty()) { Text("حفظ ورفع") }
            },
            dismissButton = { TextButton(onClick = vm::cancelImport) { Text("إلغاء") } }
        )
    }

    pickFor?.let { which ->
        val state = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { pickFor = null },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { ms ->
                        val day = Instant.ofEpochMilli(ms).atZone(ZoneOffset.UTC)
                            .toLocalDate().toEpochDay()
                        if (which == 1) startDay = day else endDay = day
                    }
                    pickFor = null
                }) { Text("تم") }
            },
            dismissButton = { TextButton(onClick = { pickFor = null }) { Text("إلغاء") } }
        ) { DatePicker(state = state) }
    }

    val times = parseTimes(timesText)
    val dosesCount = if (startDay != null && endDay != null && times != null && endDay!! >= startDay!!)
        (endDay!! - startDay!! + 1).toInt() * times.size else null

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("رفع بيانات علاج", style = MaterialTheme.typography.headlineSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { pickExcel.launch(arrayOf("*/*")) },
                enabled = !busy,
                modifier = Modifier.weight(1f)
            ) { Text("استيراد من Excel") }
            OutlinedButton(
                onClick = { saveTemplate.launch("treatments_template.xlsx") },
                modifier = Modifier.weight(1f)
            ) { Text("تحميل نموذج") }
        }
        HorizontalDivider()
        Text("أو أدخل يدويًا:", style = MaterialTheme.typography.bodyMedium)
        OutlinedTextField(patient, { patient = it }, Modifier.fillMaxWidth(),
            label = { Text("اسم المريض") }, singleLine = true)
        OutlinedTextField(bed, { bed = it }, Modifier.fillMaxWidth(),
            label = { Text("رقم السرير") }, singleLine = true)
        OutlinedTextField(drug, { drug = it }, Modifier.fillMaxWidth(),
            label = { Text("اسم الدواء") }, singleLine = true)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { pickFor = 1 }, Modifier.weight(1f)) {
                Text(startDay?.let { "من: ${LocalDate.ofEpochDay(it)}" } ?: "تاريخ البداية")
            }
            OutlinedButton(onClick = { pickFor = 2 }, Modifier.weight(1f)) {
                Text(endDay?.let { "إلى: ${LocalDate.ofEpochDay(it)}" } ?: "تاريخ النهاية")
            }
        }

        OutlinedTextField(
            timesText, { timesText = it }, Modifier.fillMaxWidth(),
            label = { Text("مواعيد الجرعات في اليوم") },
            supportingText = { Text("افصل بينهم بفاصلة، مثال: 08:00, 14:00, 20:00") },
            isError = times == null
        )

        dosesCount?.let { Text("هيتولد $it جرعة", style = MaterialTheme.typography.bodyMedium) }

        Button(
            onClick = {
                when {
                    patient.isBlank() || bed.isBlank() || drug.isBlank() ->
                        vm.toast("اكمل اسم المريض والسرير والدواء")
                    startDay == null || endDay == null ->
                        vm.toast("حدد تاريخ البداية والنهاية")
                    times == null -> vm.toast("مواعيد الجرعات غير صحيحة")
                    else -> vm.saveAndUpload(
                        patient, bed, drug,
                        LocalDate.ofEpochDay(startDay!!), LocalDate.ofEpochDay(endDay!!),
                        times
                    ) {
                        drug = ""; startDay = null; endDay = null
                    }
                }
            },
            enabled = !busy,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (busy) "جاري الحفظ..." else "حفظ ورفع") }

        if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
    }
}