package com.example.callinspector.presentation.ui

import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.callinspector.utils.loge
import kotlinx.coroutines.delay
import java.nio.ByteBuffer
import java.util.concurrent.Executors


@Composable
fun CameraPreviewScreen(
    lensFacing: Int, // CameraSelector.LENS_FACING_BACK (1) or FRONT (0)
    onTestFinished: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // State to track stages
    var isAnalyzing by remember { mutableStateOf(true) }
    var isSuccess by remember { mutableStateOf(false) }
    var countdown by remember { mutableStateOf(3) } // 3 seconds hold time

    val previewView = remember { PreviewView(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    Box(modifier = Modifier
        .height(200.dp)
        .width(200.dp)) {
        AndroidView(
            factory = {
                previewView.apply {
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // UPDATED UI: Shows "Checking" -> "Success" -> "Closing in X..."
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(32.dp)
                .background(
                    color = if (isSuccess) Color(0xFF4CAF50).copy(alpha = 0.8f) else Color.Black.copy(
                        alpha = 0.6f
                    ),
                    shape = MaterialTheme.shapes.medium
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = when {
                    isSuccess -> "Camera Working! Closing in ${countdown}s..."
                    else -> "Checking Sensor..."
                },
                color = Color.White,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }

    // 1. Camera Binding Logic
    LaunchedEffect(lensFacing) { // React if lens changes
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        var validFramesCounter = 0

        imageAnalysis.setAnalyzer(cameraExecutor) { image ->
            // If already success, just close frames and return (keep preview running)
            if (!isAnalyzing) {
                image.close()
                return@setAnalyzer
            }

            val brightness = calculateLuminosity(image)

            if (brightness > 40.0) {
                validFramesCounter++
            } else {
                validFramesCounter = 0
            }

            // Threshold met: Mark success but DO NOT close yet
            if (validFramesCounter > 20) {
                isAnalyzing = false // Stop checking brightness
                // Switch to main thread to update UI
                ContextCompat.getMainExecutor(context).execute {
                    isSuccess = true
                }
            }

            image.close()
        }

        try {
            cameraProvider.unbindAll()

            // CHANGED: Use the passed lensFacing
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()

            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector, // <--- Dynamic Selector
                preview,
                imageAnalysis
            )
        } catch (exc: Exception) {
            loge("CameraTest", "Binding failed  $exc")
            onTestFinished(false)
        }
    }

    // 2. The "Hold" Timer
    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            // Countdown loop
            while (countdown > 0) {
                delay(1000)
                countdown--
            }
            // NOW we finish
            onTestFinished(true)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
}

private fun calculateLuminosity(image: ImageProxy): Double {
    val buffer = image.planes[0].buffer
    val data = toByteArray(buffer)
    val pixels = data.map { it.toInt() and 0xFF }
    return pixels.average()
}

private fun toByteArray(buffer: ByteBuffer): ByteArray {
    buffer.rewind()
    val data = ByteArray(buffer.remaining())
    buffer.get(data)
    return data
}

