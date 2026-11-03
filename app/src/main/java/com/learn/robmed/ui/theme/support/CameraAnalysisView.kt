package com.learn.robmed.ui.theme.support


import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/** بيحتفظ باللي الصفحة دي ربطته بالكاميرا، عشان نفك ده بس (مش كل الكاميرا). */
private class CameraHolder {
    var provider: ProcessCameraProvider? = null
    var preview: Preview? = null
    var analysis: ImageAnalysis? = null

    fun release() {
        val p = provider ?: return
        analysis?.clearAnalyzer()
        val cases = listOfNotNull(preview, analysis).toTypedArray()
        if (cases.isNotEmpty()) p.unbind(*cases)
        preview = null
        analysis = null
    }
}

/**
 * مكوّن الكاميرا الوحيد في التطبيق: معاينة + تحليل الإطارات.
 *
 * - onImage: بيتنده مع كل إطار (على thread خلفي). لازم تنده finish() لما تخلص منه.
 * - onRelease: بيتنده بعد ما الكاميرا تتفك، حط فيه detector.close() / scanner.close().
 *
 * القاعدة: الصفحة الجديدة بتاخد الكاميرا (unbindAll وقت الربط)،
 * والصفحة القديمة وهي بتقفل بتفك اللي ربطته هي بس، فمش بتقتل كاميرا الصفحة الجديدة.
 */
@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraAnalysisView(
    modifier: Modifier = Modifier,
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA,
    onRelease: () -> Unit = {},
    onImage: (image: InputImage, finish: () -> Unit) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
    if (!granted) return

    val currentOnImage by rememberUpdatedState(onImage)
    val currentOnRelease by rememberUpdatedState(onRelease)
    val closed = remember { AtomicBoolean(false) }
    val executor = remember { Executors.newSingleThreadExecutor() }
    val holder = remember { CameraHolder() }

    DisposableEffect(Unit) {
        onDispose {
            closed.set(true)
            holder.release()          // 1) وقّف الإطارات
            executor.shutdown()       // 2) اقفل الـ thread
            currentOnRelease()        // 3) بعدين اقفل الـ detector
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                // TextureView: أثبت مع القص بحواف دايرية ومع الانتقال بين الصفحات
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }
            val future = ProcessCameraProvider.getInstance(ctx)
            future.addListener({
                if (closed.get()) return@addListener
                try {
                    val provider = future.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val analysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    analysis.setAnalyzer(executor) { proxy ->
                        val media = proxy.image
                        if (media == null || closed.get()) {
                            proxy.close()
                            return@setAnalyzer
                        }
                        val image = InputImage.fromMediaImage(media, proxy.imageInfo.rotationDegrees)
                        try {
                            currentOnImage(image) { proxy.close() }
                        } catch (e: Exception) {
                            Log.e("CameraAnalysisView", "analyzer failed", e)
                            proxy.close()
                        }
                    }

                    holder.provider = provider
                    holder.preview = preview
                    holder.analysis = analysis

                    provider.unbindAll()   // الصفحة الجديدة تاخد الكاميرا
                    provider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, analysis)
                } catch (e: Exception) {
                    Log.e("CameraAnalysisView", "camera bind failed", e)
                }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        }
    )
}