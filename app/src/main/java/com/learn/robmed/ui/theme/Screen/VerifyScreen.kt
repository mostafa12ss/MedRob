package com.learn.robmed.ui.theme.Screen

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.learn.robmed.tools.AndroidSpeechEngine
import com.learn.robmed.tools.PatientInfo
import com.learn.robmed.tools.RobMedSpeech
import com.learn.robmed.tools.SpeechEngine
import com.learn.robmed.tools.loadPatient
import kotlinx.coroutines.delay
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@OptIn(ExperimentalGetImage::class)
@Composable
fun VerifyScreen(onVerified: () -> Unit, onTimeout: () -> Unit, patientId: String,speaker: SpeechEngine) {
    var patient by remember { mutableStateOf<PatientInfo?>(null) }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var verified by remember { mutableStateOf(false) }
    val previewView = remember(context) {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            // TextureView: أثبت مع الانتقال بين الصفحات والقص بحواف دايرية
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }




    var faceAligned by remember { mutableStateOf(false) }

    LaunchedEffect(patientId) { loadPatient(patientId) { patient = it } }

    // بعد التعرف: اعرض النجاح ثانية ونص ثم كمّل

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { hasPermission = it }
    LaunchedEffect(Unit) { if (!hasPermission) launcher.launch(Manifest.permission.CAMERA) }

    if (!hasPermission) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("محتاجين إذن الكاميرا")
        }
        return
    }

    val done = remember { AtomicBoolean(false) }
    val closed = remember { AtomicBoolean(false) }
    val executor = remember { Executors.newSingleThreadExecutor() }
    val detector = remember {
        FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL) // لاكتشاف العين المفتوحة
                .build()
        )
    }
    var status by remember { mutableStateOf("وجّه الكاميرا نحو وجه المريض") }
    var goodFrames by remember { mutableIntStateOf(0) }

    Box(Modifier.fillMaxSize()) {

        // effect واحد: يفتح الكاميرا، ويقفل كل حاجة بالترتيب الصح
        DisposableEffect(Unit) {
            var provider: ProcessCameraProvider? = null
            var preview: Preview? = null
            var analysis: ImageAnalysis? = null

            val future = ProcessCameraProvider.getInstance(context)
            future.addListener({
                if (closed.get()) return@addListener
                try {
                    val p = future.get()
                    provider = p

                    val pv = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    preview = pv

                    val an = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                    analysis = an

                    an.setAnalyzer(executor) { proxy ->
                        val media = proxy.image
                        if (media == null || done.get() || closed.get()) {
                            proxy.close()
                            return@setAnalyzer
                        }

                        val rotation = proxy.imageInfo.rotationDegrees
                        // أبعاد الصورة بعد التدوير (نفس مساحة إحداثيات الوجه)
                        val imgW = if (rotation == 90 || rotation == 270) media.height else media.width
                        val imgH = if (rotation == 90 || rotation == 270) media.width else media.height

                        val image = InputImage.fromMediaImage(media, rotation)

                        detector.process(image)
                            .addOnSuccessListener { faces ->
                                if (closed.get()) return@addOnSuccessListener
                                val face = faces.firstOrNull()

                                val aligned = if (face != null) {
                                    val bounds = face.boundingBox
                                    val centerX = bounds.centerX().toFloat() / imgW
                                    val centerY = bounds.centerY().toFloat() / imgH
                                    val faceWidth = bounds.width().toFloat() / imgW

                                    centerX in 0.35f..0.65f &&
                                            centerY in 0.25f..0.75f &&
                                            faceWidth in 0.20f..0.65f
                                } else {
                                    false
                                }

                                val eyesOpen =
                                    face != null &&
                                            (face.leftEyeOpenProbability ?: 0f) > 0.5f &&
                                            (face.rightEyeOpenProbability ?: 0f) > 0.5f

                                faceAligned = aligned

                                status = when {
                                    face == null -> "لم يتم اكتشاف وجه"
                                    !aligned -> "اضبط وجهك داخل الإطار"
                                    !eyesOpen -> "افتح عينيك وانظر للكاميرا"
                                    else -> "ممتاز، ثبت وجهك"
                                }

                                goodFrames = if (aligned && eyesOpen) goodFrames + 1 else 0

                                if (goodFrames >= 15 && done.compareAndSet(false, true)) {
                                    verified = true
                                }
                            }
                            .addOnCompleteListener { proxy.close() }
                    }

                    p.unbindAll()   // الصفحة الجديدة تاخد الكاميرا
                    p.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_FRONT_CAMERA,
                        pv,
                        an
                    )
                } catch (e: Exception) {
                    Log.e("VerifyScreen", "camera bind failed", e)
                }
            }, ContextCompat.getMainExecutor(context))

            onDispose {
                closed.set(true)
                // 1) وقّف الإطارات  2) فك اللي ربطته أنا بس (مش unbindAll)
                analysis?.clearAnalyzer()
                val cases = listOfNotNull(preview, analysis).toTypedArray()
                if (cases.isNotEmpty()) provider?.unbind(*cases)
                // 3) بعدين اقفل الـ thread والـ detector
                executor.shutdown()
                detector.close()
            }
        }

        PatientVerifiedContent(
            verified = verified,
            name = patient?.name,
            room = null,
            bed = patient?.bedNumber,
            cameraPreview = previewView,
            cameraHint = status,
            faceAligned = faceAligned
        )
    }

    // لو محدش اتعرف عليه خلال 20 ثانية: ارجع من الأول
    LaunchedEffect(Unit) {
        delay(20_000)
        if (done.compareAndSet(false, true)) onTimeout()
    }
    LaunchedEffect(Unit) {

        RobMedSpeech.askPatientToLookAtCamera(speaker)
    }
    // طلب النظر إلى الكاميرا عند فتح الشاشة
    LaunchedEffect(speaker) {
        RobMedSpeech.askPatientToLookAtCamera(speaker)
    }

// عند نجاح التحقق
    LaunchedEffect(verified) {
        if (verified) {
            RobMedSpeech.patientVerified(speaker)
            delay(1500)
            onVerified()
        }
    }
}