package com.learn.test_exel.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.learn.test_exel.ui.viewmodel.EditData
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
private fun parseDoseTimes(text: String): List<LocalTime>? = runCatching {
    val latin = buildString {
        for (ch in text) append(
            when (ch) {
                in '٠'..'٩' -> '0' + (ch - '٠')
                in '۰'..'۹' -> '0' + (ch - '۰')
                else -> ch
            }
        )
    }
    latin.split(',', '،', ';', ' ')
        .map { it.trim() }.filter { it.isNotEmpty() }
        .map { s ->
            val p = s.split(":")
            LocalTime.of(p[0].toInt(), p.getOrNull(1)?.toInt() ?: 0)
        }.distinct()
}.getOrNull()?.takeIf { it.isNotEmpty() }
@Composable
fun EditTreatmentDialog(
    data: EditData,
    busy: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String, String, LocalDate, LocalDate, List<LocalTime>) -> Unit,
    onError: (String) -> Unit
) {
    val key = data.treatmentId
    var patient by remember(key) { mutableStateOf(data.patient) }
    var bed by remember(key) { mutableStateOf(data.bed) }
    var drug by remember(key) { mutableStateOf(data.drug) }
    var startDay by remember(key) { mutableLongStateOf(data.startDay) }
    var endDay by remember(key) { mutableLongStateOf(data.endDay) }
    var timesText by remember(key) { mutableStateOf(data.times) }
    var pickFor by remember { mutableStateOf<Int?>(null) }   // 1=بداية، 2=نهاية

    pickFor?.let { which ->
        val initial = LocalDate.ofEpochDay(if (which == 1) startDay else endDay)
            .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val state = rememberDatePickerState(initialSelectedDateMillis = initial)
        DatePickerDialog(
            onDismissRequest = { pickFor = null },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { ms ->
                        val day = java.time.Instant.ofEpochMilli(ms).atZone(ZoneOffset.UTC)
                            .toLocalDate().toEpochDay()
                        if (which == 1) startDay = day else endDay = day
                    }
                    pickFor = null
                }) { Text("تم") }
            },
            dismissButton = { TextButton(onClick = { pickFor = null }) { Text("إلغاء") } }
        ) { DatePicker(state = state) }
    }

    val times = parseDoseTimes(timesText)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تعديل العلاج") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(patient, { patient = it }, Modifier.fillMaxWidth(),
                    label = { Text("اسم المريض") }, singleLine = true,
                    supportingText = { Text("تغيير الاسم بيتطبق على كل علاجات المريض ده") })
                OutlinedTextField(bed, { bed = it }, Modifier.fillMaxWidth(),
                    label = { Text("رقم السرير") }, singleLine = true)
                OutlinedTextField(drug, { drug = it }, Modifier.fillMaxWidth(),
                    label = { Text("اسم الدواء") }, singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { pickFor = 1 }, Modifier.weight(1f)) {
                        Text("من: ${LocalDate.ofEpochDay(startDay)}")
                    }
                    OutlinedButton(onClick = { pickFor = 2 }, Modifier.weight(1f)) {
                        Text("إلى: ${LocalDate.ofEpochDay(endDay)}")
                    }
                }
                OutlinedTextField(
                    timesText, { timesText = it }, Modifier.fillMaxWidth(),
                    label = { Text("مواعيد الجرعات") },
                    isError = times == null,
                    supportingText = { Text("مثال: 08:00, 14:00, 20:00") }
                )
                Text(
                    "الجرعات اللي حالتها اتسجلت بتفضل زي ما هي، واللي لسه ما اتحددتش بتتحدّث على الجدول الجديد.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = !busy,
                onClick = {
                    when {
                        patient.isBlank() || bed.isBlank() || drug.isBlank() ->
                            onError("اكمل اسم المريض والسرير والدواء")
                        endDay < startDay -> onError("تاريخ النهاية قبل البداية")
                        times == null -> onError("مواعيد الجرعات غير صحيحة")
                        else -> onSave(
                            patient, bed, drug,
                            LocalDate.ofEpochDay(startDay), LocalDate.ofEpochDay(endDay), times
                        )
                    }
                }
            ) { Text(if (busy) "جاري الحفظ..." else "حفظ التعديل") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

