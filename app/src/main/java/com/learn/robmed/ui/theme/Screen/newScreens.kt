package com.learn.robmed.ui.theme.Screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.learn.robmed.tools.RobMedSpeech
import com.learn.robmed.tools.SpeechEngine
import com.learn.robmed.tools.Treatment
import com.learn.robmed.tools.loadTreatments
import kotlinx.coroutines.delay

@Composable
fun SuccessScreen(
    speaker: SpeechEngine,
    onDone: () -> Unit
) {
    LaunchedEffect(speaker) {
        RobMedSpeech.doseCompleted(speaker)
        delay(3000)
        onDone()
    }

    TreatmentSuccessContent()
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NextDoseScreen(
    patientId: String,
    speaker: SpeechEngine,
    onDone: () -> Unit
) {
    var treatment by remember {
        mutableStateOf<Treatment?>(null)
    }

    LaunchedEffect(patientId) {
        loadTreatments(patientId) {
            treatment = it.firstOrNull()
        }
    }

    LaunchedEffect(treatment) {
        if (treatment != null) {
            delay(5000)
            onDone()
        }
    }

    NextDoseContent2(
        drugName = treatment?.drugName.orEmpty(),
        whenText = treatment?.let {
            nextDoseText(it.doseTimes)
        }.orEmpty(),
        speaker = speaker
    )
}

// "اليوم 8:00 مساءً" أو "غدا 8:00 صباحا"
@RequiresApi(Build.VERSION_CODES.O)
fun nextDoseText(times: List<String>): String {
    if (times.isEmpty()) return "لم يتم تحديد موعد"

    val now = java.time.LocalTime.now()

    val sorted = times.mapNotNull {
        runCatching {
            java.time.LocalTime.parse(it.trim())
        }.getOrNull()
    }.sorted()

    if (sorted.isEmpty()) return "لم يتم تحديد موعد"

    val next = sorted.firstOrNull {
        it.isAfter(now)
    }

    val time = next ?: sorted.first()

    val day = if (next != null) "اليوم" else "غداً"

    val hour = if (time.hour % 12 == 0) {
        12
    } else {
        time.hour % 12
    }

    val period = if (time.hour < 12) {
        "صباحاً"
    } else {
        "مساءً"
    }

    return "$day $hour:%02d $period".format(time.minute)
}