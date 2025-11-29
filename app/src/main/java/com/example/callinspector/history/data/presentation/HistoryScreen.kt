package com.example.callinspector.history.data.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.callinspector.diagnostics.data.local.entity.DiagnosticReportEntity
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val reports by viewModel.historyState.collectAsState()

    HistoryContent(
        reports = reports,
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryContent(
    reports: List<DiagnosticReportEntity>,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Audit History") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (reports.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No reports saved yet.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(padding)
            ) {
                items(reports) { report ->
                    HistoryItemCard(report)
                }
            }
        }
    }
}

@Composable
fun HistoryItemCard(report: DiagnosticReportEntity) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = convertLongToDate(report.timestamp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Score: ${report.finalScore}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(8.dp))
                    if (!report.networkPassed) {
                        Text("⚠ Network Fail", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // The Grade Badge
            Surface(
                color = if (report.finalScore >= 70) Color(0xFFE7F6E7) else Color(0xFFFFEBEB),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = report.finalGrade,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = if (report.finalScore >= 70) Color(0xFF1B5E20) else Color(0xFFB71C1C)
                )
            }
        }
    }
}

private fun convertLongToDate(time: Long): String {
    val date = Date(time)
    val format = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
    return format.format(date)
}
@Preview(showBackground = true)
@Composable
fun HistoryScreenPreview() {
    // Create fake data for the preview
    val mockReports = listOf(
        DiagnosticReportEntity(
            timestamp = System.currentTimeMillis(),
            finalScore = 95,
            finalGrade = "A",
            micPassed = true,
            speakerPassed = true,
            networkPassed = true,
            cameraPassed = true,
            downloadSpeedMbps = 15.0,
            latencyMs = 45
        ),
        DiagnosticReportEntity(
            timestamp = System.currentTimeMillis() - 86400000,
            finalScore = 40,
            finalGrade = "F",
            micPassed = false,
            speakerPassed = true,
            networkPassed = false,
            cameraPassed = false,
            downloadSpeedMbps = 1.2,
            latencyMs = 200
        )
    )

    MaterialTheme {
        HistoryContent(
            reports = mockReports,
            onBack = {}
        )
    }
}