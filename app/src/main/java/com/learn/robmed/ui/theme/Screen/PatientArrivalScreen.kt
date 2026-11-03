package com.learn.robmed.ui.theme.Screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.learn.robmed.R
import com.learn.robmed.tools.PatientInfo
import com.learn.robmed.tools.loadPatient
import kotlinx.coroutines.delay

@Composable
fun PatientArrivalScreen(patientId: String, onContinue: () -> Unit, onBack: () -> Unit) {
    var patient by remember { mutableStateOf<PatientInfo?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(patientId) {
        loadPatient(patientId) { patient = it; loading = false }
    }

    // الانتقال التلقائي بعد التحميل
    LaunchedEffect(loading, patient) {
        if (!loading) {
            if (patient != null) {
                delay(2000L) // ينتظر ثانيتين ويروح للشاشة التالية تلقائياً
                onContinue()
            } else {
                delay(2000L)
                onBack()
            }
        }
    }

    if (!loading && patient == null) {
        ScreenBackground { ScreenTitle("المريض غير موجود") }
    } else {
        PatientArrivalContent(
            loading = loading,
            name = patient?.name,
            room = null,
            bed = patient?.bedNumber,
            bedimage = R.drawable.bedimage
        )
    }
}