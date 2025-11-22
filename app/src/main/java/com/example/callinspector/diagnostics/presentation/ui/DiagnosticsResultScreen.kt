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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.callinspector.diagnostics.presentation.viewModel.DiagnosticsViewModel
import com.example.callinspector.utils.loge

@Composable
fun DiagnosticsResultScreen(
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DiagnosticsViewModel
)
  {
    val state by viewModel.uiState.collectAsState()
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



            Spacer(Modifier.height(16.dp))

            Button(onClick = onBackToHome) {
                Text("Back to home")
            }
        }
    }
}

@Composable
private fun ResultRow(
    label: String,
    success: Boolean?
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
        Text(text = label, style = MaterialTheme.typography.titleMedium)
        Text(text = statusText, color = statusColor)
    }
}
@Preview(showBackground = true)
@Composable
private fun DiagnosticsResultScreenPreview() {
    DiagnosticsResultScreen(onBackToHome = {}, viewModel = viewModel())
}