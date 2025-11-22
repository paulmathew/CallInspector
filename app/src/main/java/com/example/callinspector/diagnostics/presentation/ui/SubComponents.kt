package com.example.callinspector.diagnostics.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun SpeakerQuestionSection(
    volume: Int,
    maxVolume: Int,
    awaitingConfirmation: Boolean,
    onHeard: () -> Unit,
    onNotHeard: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (!awaitingConfirmation) {
            Text(
                text = "Playing test sound…",
                style = MaterialTheme.typography.bodyMedium
            )
            return
        }

        val volumePercent =
            if (maxVolume > 0) (volume * 100 / maxVolume) else 0

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Did you hear the test sound?",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Current media volume: $volumePercent% ($volume / $maxVolume)",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(12.dp))

        androidx.compose.foundation.layout.Row(
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onHeard) {
                Text("Yes, I heard it")
            }
            Button(onClick = onNotHeard) {
                Text("No, it's too quiet")
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            text = "If you didn't hear it, increase your device's media volume using the hardware buttons and run the test again.",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

@Preview()
@Composable
fun showSpeakerQuestionSection() {
    SpeakerQuestionSection(
        awaitingConfirmation = true,
        onHeard = {},
        onNotHeard = {},
        volume = 0,
        maxVolume = 0
    )
}
