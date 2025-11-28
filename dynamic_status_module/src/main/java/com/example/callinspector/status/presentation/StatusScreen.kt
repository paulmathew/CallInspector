package com.example.callinspector.status.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
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
import com.example.callinspector.status.data.model.ServiceStatus

// 1. The Entry Point (Stateful) - Used by Navigation
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusScreen(
    onBackClick: () -> Unit,
    viewModel: StatusViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    // Pass state down to the stateless composable
    StatusScreenContent(
        state = state,
        onRefresh = { viewModel.loadStatuses() },
        onBackClick = {onBackClick()}
    )
}

// 2. The UI Logic (Stateless) - Used by Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusScreenContent(
    state: StatusUiState,
    onRefresh: () -> Unit,
    onBackClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Service Status") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (state) {
                is StatusUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is StatusUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is StatusUiState.Success -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.services) { service ->
                            ServiceStatusCard(service)
                        }
                    }
                }
            }
        }
    }
}

// 3. Components
@Composable
fun ServiceStatusCard(service: ServiceStatus) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(service.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Latency: ${service.latency}ms", style = MaterialTheme.typography.bodySmall)
            }

            StatusIndicator(service.status)
        }
    }
}

@Composable
fun StatusIndicator(status: String) {
    val (color, text) = when (status.lowercase()) {
        "operational" -> Color.Green to "Active"
        "degraded" -> Color(0xFFFFC107) to "Slow" // Amber
        else -> Color.Red to "Down"
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(end = 8.dp))
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
    }
}

// 4. THE PREVIEW (Uses Stateless Content)
@Preview(showBackground = true)
@Composable
fun PreviewStatusScreen() {
    // We create fake data here so we don't need the ViewModel
    val mockData = listOf(
        ServiceStatus("1", "Zoom", "operational", 45, "Now"),
        ServiceStatus("2", "Teams", "degraded", 120, "Now"),
        ServiceStatus("3", "Slack", "outage", 0, "Now")
    )

    StatusScreenContent(
        state = StatusUiState.Success(mockData),
        onRefresh = {},
        onBackClick = {}
    )
}