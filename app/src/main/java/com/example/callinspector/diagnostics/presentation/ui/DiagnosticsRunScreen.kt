package com.example.callinspector.diagnostics.presentation.ui

import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.callinspector.diagnostics.presentation.viewModel.DiagnosticStep
import com.example.callinspector.diagnostics.presentation.viewModel.DiagnosticsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow

@Composable
fun DiagnosticsRunScreen(
    onGoToResult: () -> Unit,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DiagnosticsViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // -- for auto starting ----
//    LaunchedEffect(Unit) {
//        viewModel.startDiagnostics()
//    }

    LaunchedEffect(state.currentStep) {
        if(state.currentStep is DiagnosticStep.Completed){
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

            if (state.currentStep is DiagnosticStep.SpeakerTest) {
                SpeakerQuestionSection(
                    volume = state.speakerVolume,
                    maxVolume = state.speakerMaxVolume,
                    awaitingConfirmation = state.awaitingSpeakerConfirmation,
                    onHeard = { viewModel.onSpeakerHeard(true) },
                    onNotHeard = { viewModel.onSpeakerHeard(false) }
                )
            }

            if (!state.isRunning && state.currentStep !is DiagnosticStep.Completed) {
                Button(onClick = { viewModel.startDiagnostics() }) {
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
        DiagnosticStep.CameraTest -> "Camera test"
        DiagnosticStep.DeviceTest -> "Device capability test"
        DiagnosticStep.Completed -> "All tests completed"
    }


@Preview(showBackground = true)
@Composable
private fun DiagnosticsRunScreenPreview() {
    DiagnosticsRunScreen(
        onGoToResult = {},
        onBackToHome = {},
        viewModel = viewModel()
    )
}
