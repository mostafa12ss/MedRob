package com.learn.test_exel.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.learn.test_exel.Backend.data.DoseSummaryRow
import com.learn.test_exel.ui.viewmodel.HospitalViewModel

@Composable
fun DataScreen(vm: HospitalViewModel) {
    val rows by vm.summary.collectAsState()
    val busy by vm.busy.collectAsState()
    val editing by vm.editing.collectAsState()
    var confirmClear by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        )
    ) { uri -> uri?.let { vm.exportExcel(it) } }

    editing?.let { e ->
        EditTreatmentDialog(
            data = e,
            busy = busy,
            onDismiss = vm::cancelEdit,
            onSave = { patient, bed, drug, start, end, times ->
                vm.saveEdit(e.treatmentId, patient, bed, drug, start, end, times)
            },
            onError = vm::toast
        )
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("مسح البيانات من الجهاز؟") },
            text = {
                Text(
                    "هيتمسح كل المرضى والعلاجات والجرعات من التليفون. " +
                            "البيانات اللي على Firebase مش هتتأثر، ولو سحبت تاني هترجع. " +
                            "أي بيانات لسه ما اترفعتش هتضيع."
                )
            },
            confirmButton = {
                TextButton(onClick = { confirmClear = false; vm.clearLocalData() }) {
                    Text("مسح", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("إلغاء") } }
        )
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("عرض البيانات", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { vm.pullFromFirebase() },
                enabled = !busy,
                modifier = Modifier.weight(1f)
            ) { Text("سحب من Firebase") }

            OutlinedButton(
                onClick = { exportLauncher.launch("hospital_summary.xlsx") },
                enabled = rows.isNotEmpty() && !busy,
                modifier = Modifier.weight(1f)
            ) { Text("تصدير Excel") }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { vm.reuploadAll() }, enabled = !busy) { Text("إعادة رفع كل البيانات") }
            TextButton(
                onClick = { confirmClear = true },
                enabled = rows.isNotEmpty() && !busy
            ) { Text("مسح بيانات الجهاز", color = MaterialTheme.colorScheme.error) }
        }

        if (busy) LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 4.dp))
        Spacer(Modifier.height(8.dp))

        if (rows.isEmpty()) {
            Text("لا توجد بيانات. أضف علاج من تبويب الرفع، أو اضغط سحب من Firebase.")
        } else {
            Text("اضغط على أي صف لتعديله", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(4.dp))
            TableLine("المريض", "سرير", "الدواء", "أخد", "ما أخدش", "لسه", header = true)
            HorizontalDivider()
            LazyColumn {
                items(rows, key = { it.treatmentId }) { r ->
                    SummaryLine(r, onClick = { vm.startEdit(r.treatmentId) })
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun SummaryLine(r: DoseSummaryRow, onClick: () -> Unit) = TableLine(
    r.patientName, r.bedNumber, r.drugName,
    r.takenCount.toString(), r.missedCount.toString(), r.pendingCount.toString(),
    modifier = Modifier.clickable(onClick = onClick)
)

@Composable
private fun TableLine(
    a: String, b: String, c: String, d: String, e: String, f: String,
    header: Boolean = false,
    modifier: Modifier = Modifier
) {
    val style = MaterialTheme.typography.bodySmall
    val weight = if (header) FontWeight.Bold else FontWeight.Normal
    Row(
        modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(a, Modifier.weight(2f), style = style, fontWeight = weight)
        Text(b, Modifier.weight(1f), style = style, fontWeight = weight)
        Text(c, Modifier.weight(2f), style = style, fontWeight = weight)
        Text(d, Modifier.weight(1f), style = style, fontWeight = weight)
        Text(e, Modifier.weight(1.2f), style = style, fontWeight = weight)
        Text(f, Modifier.weight(1f), style = style, fontWeight = weight)
    }
}