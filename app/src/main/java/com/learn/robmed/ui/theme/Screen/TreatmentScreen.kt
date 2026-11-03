package com.learn.robmed.ui.theme.Screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.learn.robmed.tools.ScanResult
import com.learn.robmed.tools.Treatment
import com.learn.robmed.tools.loadTreatments
import com.learn.robmed.tools.scheduledMillis
import kotlinx.coroutines.delay
import com.learn.robmed.tools.RobMedSpeech
import com.learn.robmed.tools.SpeechEngine


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TreatmentScreen(
    patientId: String,
    speaker: SpeechEngine,
    onFinished: (drugName: String, doseTime: String, drugId: String) -> Unit,
    onFailed: () -> Unit
) {
    var list by remember { mutableStateOf<List<Treatment>?>(null) }
    var message by remember { mutableStateOf("وجه الكاميرا نحو QR الدواء") }
    var failed by remember { mutableStateOf(false) }

    // متغير لتحديث الماسح وإجبار الكاميرا على إعادة التركيز
    var scanKey by remember { mutableIntStateOf(0) }

    // جلب أدوية المريض عند فتح الشاشة
    LaunchedEffect(patientId) {
        loadTreatments(patientId) { list = it }
    }

    // إذا حدث خطأ، ننتظر قليلاً ثم نرجع للخلف
    LaunchedEffect(failed) {
        if (failed) {
            delay(2000)
            onFailed()
        }
    }

    // مؤقت ذكي لإعادة تنشيط الـ Scanner لو حصل بطء أو توقف مؤقت
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000) // كل 5 ثواني يحدث مفتاح الماسح لتفادي تعليق الإطارات
            scanKey++
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("امسح QR الدواء الخاص بالمريض", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))

        when {
            list == null -> CircularProgressIndicator()
            list!!.isEmpty() -> Text("لا يوجد علاج ساري لهذا المريض")
            else -> Text(message, style = MaterialTheme.typography.titleMedium)
        }

        Spacer(Modifier.height(16.dp))

        Box(Modifier.weight(1f)) {
            // تشغيل ماسح الـ QR مع تمرير الـ scanKey المحدث
            QrScannerScreen(scanKey) { result ->
                if (failed) return@QrScannerScreen

                when (result) {
                    is ScanResult.Medication -> {
                        val treatment = list?.firstOrNull { it.id == result.id }
                        if (treatment == null) {
                            message = "هذا الدواء غير مطابق لعلاج المريض ✗"
                            RobMedSpeech.error(speaker)
                            failed = true
                        } else {
                            val time = nearestDoseTime(treatment.doseTimes)
                            RobMedSpeech.announceMedication(
                                speaker = speaker,
                                drugName = treatment.drugName,
                                doseTime = time
                            )
                            // نجح المسح تماماً، ننتقل للشاشة التالية
                            onFinished(treatment.drugName, time, treatment.id)
                        }
                    }
                    is ScanResult.Patient -> {
                        message = "هذا QR مريض، يرجى مسح QR الدواء ✗"
                        scanKey++
                    }
                    else -> {
                        message = "QR غير معروف، حاول مرة أخرى"
                        scanKey++
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun nearestDoseTime(times: List<String>): String {
    val now = System.currentTimeMillis()
    return times.minByOrNull { kotlin.math.abs(scheduledMillis(it) - now) } ?: times.first()
}