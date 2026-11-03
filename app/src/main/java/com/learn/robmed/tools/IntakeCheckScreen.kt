package com.learn.robmed.ui.theme.Screen

import android.os.SystemClock
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import com.learn.robmed.tools.HandToMouthDetector
import com.learn.robmed.tools.IntakeState
import com.learn.robmed.tools.RobMedSpeech
import com.learn.robmed.tools.SpeechEngine
import com.learn.robmed.ui.theme.support.CameraAnalysisView
import kotlinx.coroutines.delay

/** معاينة الكاميرا + رصد نقاط الجسم. onPose بيتنده على الـ main thread. */
@Composable
fun PoseCameraPreview(
    modifier: Modifier = Modifier,
    onPose: (Pose?) -> Unit
) {
    val currentOnPose by rememberUpdatedState(onPose)
    val detector = remember {
        PoseDetection.getClient(
            PoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                .build()
        )
    }

    CameraAnalysisView(
        modifier = modifier,
        onRelease = { detector.close() }
    ) { image, finish ->
        detector.process(image)
            .addOnSuccessListener { pose -> currentOnPose(pose) }
            .addOnFailureListener { currentOnPose(null) }
            .addOnCompleteListener { finish() }
    }
}

/**
 * صفحة "جارى التأكد من أخذ الدواء".
 * تنجح لما يترصد حركة اليد للفم، وتفشل (ترجع من الأول) لو عدّى [timeoutMs] من غير رصد.
 */
@Composable
fun IntakeCheckScreen(
    speaker: SpeechEngine,
    onSuccess: () -> Unit,
    onFailed: () -> Unit,
    timeoutMs: Long = 30_000L
) {
    val detector = remember { HandToMouthDetector() }
    var state by remember { mutableStateOf(IntakeState()) }
    var done by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(timeoutMs)
        if (!done) { done = true; onFailed() }
    }

    LaunchedEffect(state.completed) {
        if (state.completed && !done) {
            done = true
            delay(1200)
            onSuccess()
        }
    }
    LaunchedEffect(speaker) {
        RobMedSpeech.askToTakeMedication(
            speaker = speaker,
            drugName = ""
        )
    }

    val status = when {
        state.completed -> "تم رصد حركة تناول الدواء ✓"
        state.handNear -> "جاري الرصد..."
        else -> "ارفع يدك نحو فمك لتناول الدواء"
    }

    MedicationCheckContent(
        status = status,
        preview = {
            PoseCameraPreview(Modifier.fillMaxSize()) { pose ->
                if (!done) state = detector.update(pose, SystemClock.elapsedRealtime())
            }
        }
    )
}