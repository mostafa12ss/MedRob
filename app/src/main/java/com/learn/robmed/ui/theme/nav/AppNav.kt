package com.learn.robmed.ui.theme.nav

import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.learn.robmed.tools.ScanResult
import com.learn.robmed.tools.SpeechEngine
import com.learn.robmed.tools.logDose
import com.learn.robmed.tools.scheduledMillis
import com.learn.robmed.ui.theme.Screen.* // استيراد الشاشات
import com.learn.robmed.ui.theme.support.ForceOrientation
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.DisposableEffect
import com.learn.robmed.tools.AndroidSpeechEngine
import com.learn.robmed.tools.RobMedSpeech

// نصوص الـ routes في مكان واحد، عشان popUpTo يطابقها بالظبط
private const val ROUTE_PREVIEW =
    "medication_preview/{id}?drugName={drugName}&doseTime={doseTime}&drugId={drugId}"
private const val ROUTE_INTAKE =
    "intake_check/{id}?drugId={drugId}&doseTime={doseTime}&drugName={drugName}"
private const val ROUTE_FINAL =
    "final_next_dose/{id}?drugName={drugName}&doseTime={doseTime}"

private fun stringArg(name: String, optional: Boolean = true) =
    navArgument(name) {
        type = NavType.StringType
        if (optional) defaultValue = ""
    }

private val medArgs = listOf(
    stringArg("id", optional = false),
    stringArg("drugName"),
    stringArg("doseTime"),
    stringArg("drugId")
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation() {
    val nav = rememberNavController()
    val restart = { nav.popBackStack("scanner", inclusive = false) }
    val context = LocalContext.current

    val speaker = remember {
        AndroidSpeechEngine(context)
    }

    DisposableEffect(speaker) {
        onDispose {
            speaker.release()
        }
    }
    NavHost(
        navController = nav,
        startDestination = "welcome",
        // بدون أنيميشن: صفحتين بكاميرا ما يظهروش مع بعض أثناء الانتقال
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {

        // 1. شاشة الترحيب
        composable("welcome") {
            ForceOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
            WelcomeScreen(
                speaker = speaker,
                onFinished = {
                    nav.navigate("scanner") {
                        popUpTo("welcome") {
                            inclusive = true
                        }
                    }
                }
            )
        }

        // 2. مسح QR المريض
        composable("scanner") {
            var scanKey by remember { mutableIntStateOf(0) }
            var msg by remember { mutableStateOf("") }

            LaunchedEffect(msg) {
                if (msg.isNotEmpty()) { delay(1500); msg = ""; scanKey++ }
            }

            Box(Modifier.fillMaxSize()) {
                QrScannerScreen(scanKey) { result ->
                    when (result) {
                        is ScanResult.Patient -> nav.navigate("arrival/${Uri.encode(result.id)}")
                        is ScanResult.Medication -> msg = "امسح QR المريض الأول"
                        is ScanResult.Unknown -> msg = "QR غير معروف، حاول مرة أخرى"
                    }
                }
                if (msg.isNotEmpty()) {
                    Text(
                        msg, color = Color.White,
                        modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                    )
                }
            }
        }

        // 3. شاشة وصول المريض
        composable("arrival/{id}") { e ->
            val id = e.arguments?.getString("id").orEmpty()
            PatientArrivalScreen(
                patientId = id,
                onContinue = { nav.navigate("verify/${Uri.encode(id)}") },
                onBack = { restart() }
            )
        }

        // 4. شاشة التحقق (وجه)
        composable("verify/{id}") { e ->
            val id = e.arguments?.getString("id").orEmpty()
            VerifyScreen(
                patientId = id,
                speaker = speaker,
                onVerified = {
                    nav.navigate("treatment/${Uri.encode(id)}") {
                        popUpTo("arrival/{id}") {
                            inclusive = true
                        }
                    }
                },
                onTimeout = {
                    RobMedSpeech.error(speaker)
                    restart()
                }
            )
        }

        // 5. شاشة العلاج ومسح QR الدواء
        composable("treatment/{id}") { e ->
            val id = e.arguments?.getString("id").orEmpty()
            TreatmentScreen(
                    patientId = id,
            speaker = speaker,
            onFinished = { drugName, doseTime, drugId ->
                nav.navigate(
                    "medication_preview/${Uri.encode(id)}" +
                            "?drugName=${Uri.encode(drugName)}" +
                            "&doseTime=${Uri.encode(doseTime)}" +
                            "&drugId=${Uri.encode(drugId)}"
                ) {
                    popUpTo("treatment/{id}") {
                        inclusive = true
                    }
                }
            },
            onFailed = {
                restart()
            }
            )
        }

        // 6. معاينة الدواء الممسوح
        composable(ROUTE_PREVIEW, arguments = medArgs) { e ->
            val id = e.arguments?.getString("id").orEmpty()
            val drugName = e.arguments?.getString("drugName").orEmpty()
            val doseTime = e.arguments?.getString("doseTime").orEmpty()
            val drugId = e.arguments?.getString("drugId").orEmpty()

            NextDoseContent1(
                drugName = drugName,
                speaker = speaker
            )
            LaunchedEffect(Unit) {
                delay(2000)
                nav.navigate(
                    "intake_check/${Uri.encode(id)}" +
                            "?drugId=${Uri.encode(drugId)}" +
                            "&doseTime=${Uri.encode(doseTime)}" +
                            "&drugName=${Uri.encode(drugName)}"
                ) {
                    popUpTo(ROUTE_PREVIEW) { inclusive = true }   // ما يرجعش لها بزرار الرجوع
                }
            }

        }

        // 7. رصد حركة اليد للفم
        composable(ROUTE_INTAKE, arguments = medArgs) { e ->
            val id = e.arguments?.getString("id").orEmpty()
            val drugId = e.arguments?.getString("drugId").orEmpty()
            val doseTime = e.arguments?.getString("doseTime").orEmpty()
            val drugName = e.arguments?.getString("drugName").orEmpty()

            // لو تسجيل الجرعة ما ردش (مثلًا أوفلاين) نرجع للأول بدل ما نعلق
            var logging by remember { mutableStateOf(false) }
            LaunchedEffect(logging) {
                if (logging) { delay(8_000); restart() }
            }

            IntakeCheckScreen(
                speaker = speaker,

                onSuccess = {
                    val scheduled = runCatching {
                        scheduledMillis(doseTime)
                    }.getOrNull()

                    if (scheduled == null || drugId.isEmpty()) {
                        RobMedSpeech.error(speaker)
                        restart()
                    } else {
                        logging = true

                        logDose(drugId, scheduled) { success ->
                            if (success) {
                                nav.navigate(
                                    "final_next_dose/${Uri.encode(id)}" +
                                            "?drugName=${Uri.encode(drugName)}" +
                                            "&doseTime=${Uri.encode(doseTime)}"
                                ) {
                                    popUpTo("scanner")
                                }
                            } else {
                                RobMedSpeech.error(speaker)
                                restart()
                            }
                        }
                    }
                },

                onFailed = {
                    RobMedSpeech.error(speaker)
                    restart()
                },

                timeoutMs = 30_000L
            )
        }

        // 8. الشاشة النهائية
        // 8. شاشة الدواء القادم NEXT DRUG 2
        composable(ROUTE_FINAL, arguments = medArgs.filter { it.name != "drugId" }) { e ->

            val drugName = e.arguments?.getString("drugName").orEmpty()
            val doseTime = e.arguments?.getString("doseTime").orEmpty()

            // عرض الدواء القادم
            NextDoseContent2(
                drugName = drugName,
                whenText = doseTime,
                speaker = speaker
            )

            LaunchedEffect(Unit) {
                delay(4000)

                // الانتقال إلى شاشة SUCCESS بعد عرض الدواء القادم
                nav.navigate("success") {
                    popUpTo(ROUTE_FINAL) {
                        inclusive = true
                    }
                }
            }
        }
        // 9. شاشة النجاح النهائية
        composable("success") {
            SuccessScreen(
                speaker = speaker,
                onDone = {
                    restart()
                }
            )
        }
    }
}