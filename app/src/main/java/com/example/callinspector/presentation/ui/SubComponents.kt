package com.example.callinspector.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.callinspector.diagnostics.domain.model.DeviceHealth
import com.example.callinspector.presentation.viewModel.DiagnosticsUiState
import com.example.callinspector.ui.theme.CallInspectorTheme

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

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
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

@Preview(showBackground = true)
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

@Composable
fun NetworkStatusCard(state: DiagnosticsUiState) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Network Diagnostics", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                NetworkStatItem("Latency", "${state.networkLatencyMs ?: 0} ms")
                NetworkStatItem("Jitter", "${state.networkJitterMs ?: 0} ms")
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                val speedMbps = String.format("%.1f", (state.networkDownloadKbps ?: 0) / 1000.0)
                NetworkStatItem("Download", "$speedMbps Mbps")
                NetworkStatItem("Packet Loss", "${state.networkPacketLossPercent ?: 0}%")
            }

            if (state.isRunning) {
                Spacer(Modifier.height(16.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text("Analyzing connection quality...", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun NetworkStatItem(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

// Preview 1: The "Active" state (Spinner showing, partial data)
@Preview(showBackground = true, name = "Network Test Running")
@Composable
fun PreviewNetworkStatus_Running() {
    CallInspectorTheme {
        NetworkStatusCard(
            state = DiagnosticsUiState(
                isRunning = true,
                networkLatencyMs = 45,
                networkJitterMs = 12,
                networkDownloadKbps = 5400, // 5.4 Mbps
                networkPacketLossPercent = 0
            )
        )
    }
}

// Preview 2: The "Finished" state (Static results, no spinner)
@Preview(showBackground = true, name = "Network Test Completed")
@Composable
fun PreviewNetworkStatus_Completed() {
    CallInspectorTheme {
        NetworkStatusCard(
            state = DiagnosticsUiState(
                isRunning = false,
                networkLatencyMs = 42,
                networkJitterMs = 3,
                networkDownloadKbps = 15400, // 15.4 Mbps
                networkPacketLossPercent = 0
            )
        )
    }
}

@Composable
fun DeviceSpecsCard(health: DeviceHealth) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Device Capabilities", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))

            SpecRow("Model", "${health.brand} ${health.model}")
            SpecRow("Android", "Version ${health.androidVersion}")
            SpecRow("RAM", "${health.ramAvailableGb}GB free / ${health.ramTotalGb}GB")
            SpecRow("Storage", "${health.storageFreeGb}GB free / ${health.storageTotalGb}GB")
            SpecRow("Battery", "${health.batteryLevel}% ${if(health.isCharging) "⚡" else ""}")

            Spacer(Modifier.height(8.dp))
            Text("Sensors", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            val sensors = mutableListOf<String>()
            if (health.hasGyroscope) sensors.add("Gyro")
            if (health.hasAccelerometer) sensors.add("Accel")
            if (health.hasMagnetometer) sensors.add("Magnet")
            Text(sensors.joinToString(", "), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
    }
}
@Preview(showBackground = true, name = "Device Specs Card")
@Composable
fun PreviewDeviceSpecsCard() {

    val mockHealth = DeviceHealth(
        brand = "GOOGLE",
        model = "Pixel 7 Pro",
        androidVersion = "14",
        coreCount = 8,
        ramTotalGb = 12.0,
        ramAvailableGb = 4.5,
        storageTotalGb = 256.0,
        storageFreeGb = 112.4,
        batteryLevel = 78,
        isCharging = true,
        hasGyroscope = true,
        hasAccelerometer = true,
        hasMagnetometer = true
    )


    CallInspectorTheme {
        DeviceSpecsCard(health = mockHealth)
    }
}

