package com.example.callinspector.diagnostics.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.callinspector.diagnostics.presentation.viewModel.DiagnosticsUiState
import com.example.callinspector.diagnostics.presentation.viewModel.DiagnosticsViewModel
import com.example.callinspector.utils.loge

@Composable
fun DiagnosticsResultScreen(
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DiagnosticsViewModel
) {
    // 1. Collect state from ViewModel
    val state by viewModel.uiState.collectAsState()

    // 2. Pass plain data to the content composable
    DiagnosticsResultContent(
        state = state,
        onBackToHome = onBackToHome,
        modifier = modifier
    )
}
@Composable
fun DiagnosticsResultContent(
    state: DiagnosticsUiState,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                text = "Results",
                style = MaterialTheme.typography.headlineMedium
            )

            ResultRow(
                label = "Microphone",
                success = state.micSuccess
            )

            Spacer(Modifier.height(12.dp))
            ResultRow(
                label = "Speaker",
                success = state.speakerSuccess
            )
            Spacer(Modifier.height(12.dp))
            // --- NETWORK (With Technical Details) ---
            // Convert Kbps back to Mbps for display
            val downloadMbps = (state.networkDownloadKbps ?: 0) / 1000.0
            val networkDetails = if (state.networkSuccess != null) {
                "Latency: ${state.networkLatencyMs}ms  |  Jitter: ${state.networkJitterMs}ms\n" +
                        "Download: ${String.format("%.1f", downloadMbps)} Mbps  |  Loss: ${state.networkPacketLossPercent}%"
            } else null

            ResultRow(
                label = "Network Quality",
                success = state.networkSuccess,
                details = networkDetails
            )
            Spacer(Modifier.height(12.dp))

            ResultRow(
                label = "Back Camera",
                success = state.backCameraSuccess
            )
            Spacer(Modifier.height(12.dp))
            // --- FRONT CAMERA ---
            ResultRow(
                label = "Front Camera",
                success = state.frontCameraSuccess
            )
            Spacer(Modifier.height(32.dp))


            Button(onClick = onBackToHome) {
                Text("Back to home")
            }
        }
    }
}

@Composable
private fun ResultRow(
    label: String,
    success: Boolean?,
    details: String? = null
) {
    val statusText = when (success) {
        null -> "Not run"
        true -> "Pass"
        false -> "Fail"
    }

    val statusColor = when (success) {
        null -> MaterialTheme.colorScheme.onSurfaceVariant
        true -> MaterialTheme.colorScheme.primary
        false -> MaterialTheme.colorScheme.error
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Side: Label + Details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            // Only show details if they exist (Senior UI polish)
            if (details != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = details,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Right Side: Status (PASS/FAIL)
        Text(
            text = statusText,
            color = statusColor,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
@Preview(showBackground = true)
@Composable
private fun DiagnosticsResultScreenPreview() {
    // Create a dummy state object
    val dummyState = DiagnosticsUiState(
        micSuccess = true,speakerSuccess = false,
        networkSuccess = true,
        networkLatencyMs = 45,
        networkJitterMs = 5,
        networkDownloadKbps = 15000, // 15 Mbps
        networkPacketLossPercent = 0
    )

    DiagnosticsResultContent(
        state = dummyState,
        onBackToHome = {}
    )
}