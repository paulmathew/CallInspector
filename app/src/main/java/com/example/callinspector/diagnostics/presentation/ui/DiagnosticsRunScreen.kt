package com.example.callinspector.diagnostics.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun DiagnosticsRunScreen(
    onGoToResult: () -> Unit,
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
                text = "Running diagnostics…",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(Modifier.height(16.dp))

            Button(onClick = onGoToResult) {
                Text("Go to results")
            }

            Spacer(Modifier.height(8.dp))

            Button(onClick = onBackToHome) {
                Text("Back to home")
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
private fun DiagnosticsRunScreenPreview() {
    DiagnosticsRunScreen(
        onGoToResult = {},
        onBackToHome = {}
    )
}