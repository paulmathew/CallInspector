package com.example.callinspector.diagnostics.presentation.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.layout.VerticalAlignmentLine
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.callinspector.diagnostics.presentation.viewModel.DiagnosticStep
import com.example.callinspector.diagnostics.presentation.viewModel.DiagnosticsUiState
import com.example.callinspector.diagnostics.presentation.viewModel.DiagnosticsViewModel

@Composable
fun DiagnosticsRunScreen(
    onGoToResult: () -> Unit,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DiagnosticsViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Navigation Logic
    LaunchedEffect(state.currentStep) {
        if (state.currentStep is DiagnosticStep.Completed) {
            onGoToResult()
        }
    }

    // Pass state and events to the pure UI composable
    DiagnosticsRunContent(
        state = state,
        onGoToResult = onGoToResult,
        onBackToHome = onBackToHome,
        onStartDiagnostics = { viewModel.startDiagnostics() },
        onSpeakerHeard = { heard -> viewModel.onSpeakerHeard(heard) },
        onBackCameraResult = { isPassed -> viewModel.onBackCameraResult(isPassed) },
        onFrontCameraResult = { isPassed -> viewModel.onFrontCameraResult(isPassed) },
        modifier = modifier
    )
}

@Composable
fun DiagnosticsRunContent(
    state: DiagnosticsUiState,
    onGoToResult: () -> Unit,
    onBackToHome: () -> Unit,
    onStartDiagnostics: () -> Unit,
    onSpeakerHeard: (Boolean) -> Unit,
    onBackCameraResult: (Boolean) -> Unit,
    onFrontCameraResult: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {

    // -- for auto starting ----
//    LaunchedEffect(Unit) {
//        viewModel.startDiagnostics()
//    }

    LaunchedEffect(state.currentStep) {
        if (state.currentStep is DiagnosticStep.Completed) {
            onGoToResult()
        }
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Running diagnostics…",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(Modifier.height(16.dp))
            Text(
                text = stepLabel(step = state.currentStep),
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(16.dp))

            if (state.isRunning) {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
            }

            if (state.currentStep is DiagnosticStep.SpeakerTest && !state.isRunning) {
                SpeakerQuestionSection(
                    volume = state.speakerVolume,
                    maxVolume = state.speakerMaxVolume,
                    awaitingConfirmation = state.awaitingSpeakerConfirmation,
                    onHeard = { onSpeakerHeard(true) },
                    onNotHeard = { onSpeakerHeard(false) }
                )
            }
            if (state.currentStep is DiagnosticStep.NetworkTest) {
                NetworkStatusCard(state)
            }

            // 1. Back Camera Step
            if (state.currentStep is DiagnosticStep.BackCameraTest) {
                Spacer(Modifier.height(16.dp))
                CameraWrapper(
                    lensFacing = CameraSelector.LENS_FACING_BACK,
                    label = "Testing Back Camera...",
                    onResult = { onBackCameraResult(it) }
                )
                Spacer(Modifier.height(16.dp))
            }

            // 2. Front Camera Step
            if (state.currentStep is DiagnosticStep.FrontCameraTest) {
                Spacer(Modifier.height(16.dp))
                CameraWrapper(
                    lensFacing = CameraSelector.LENS_FACING_FRONT,
                    label = "Testing Front Camera...",
                    onResult = { onFrontCameraResult(it) }
                )
                Spacer(Modifier.height(16.dp))
            }

            if (state.currentStep is DiagnosticStep.DeviceTest) {
                if (state.deviceHealth != null) {
                    DeviceSpecsCard(state.deviceHealth)
                } else {
                    Text("Scanning Device Hardware...", modifier = Modifier.padding(top = 8.dp))
                }
                Spacer(Modifier.height(16.dp))
            }




            if (!state.isRunning && state.currentStep !is DiagnosticStep.Completed) {
                Button(onClick = { onStartDiagnostics() }) {
                    Text("Start")
                }
            } else if (!state.isRunning) {
                Button(onClick = onGoToResult) {
                    Text("Go to results")
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(onClick = onBackToHome) {
                Text("Back to home")
            }
        }

    }

}

@Composable
private fun stepLabel(step: DiagnosticStep): String =
    when (step) {
        DiagnosticStep.Idle -> "Idle – not started"
        DiagnosticStep.MicTest -> "Microphone test"
        DiagnosticStep.SpeakerTest -> "Speaker test"
        DiagnosticStep.NetworkTest -> "Network test"
        DiagnosticStep.BackCameraTest -> "Back Camera test"
        DiagnosticStep.FrontCameraTest -> "Front Camera test"
        DiagnosticStep.DeviceTest -> "Device capability test"
        DiagnosticStep.Completed -> "All tests completed"
    }


@Preview(showBackground = true)
@Composable
private fun DiagnosticsRunScreenPreview() {
    // Create a dummy state
    val dummyState = DiagnosticsUiState(
        currentStep = DiagnosticStep.BackCameraTest,
        isRunning = true,
        networkDownloadKbps = 15000 // Example speed
    )

    DiagnosticsRunContent(
        state = dummyState,
        onGoToResult = {},
        onBackToHome = {},
        onStartDiagnostics = {},
        onSpeakerHeard = {},
        onBackCameraResult = {},
        onFrontCameraResult = {},
    )
}

// Helper to avoid duplicating the permission check logic
@Composable
fun CameraWrapper(lensFacing: Int, label: String, onResult: (Boolean) -> Unit) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
            if (!granted) {
                onResult(false)
            }
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    if (hasCameraPermission) {
        Column(
            Modifier.size(300.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                label,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))

            CameraPreviewScreen(
                lensFacing = lensFacing,
                onTestFinished = onResult
            )

        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CameraPreview() {
    CameraPreviewScreen(lensFacing = 1, onTestFinished = {})
}
