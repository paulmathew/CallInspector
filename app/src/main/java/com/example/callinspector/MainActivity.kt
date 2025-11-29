package com.example.callinspector

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.callinspector.presentation.viewModel.DiagnosticsViewModel
import com.example.callinspector.navigation.AppNavGraph
import com.example.callinspector.ui.theme.CallInspectorTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShowInspectorApp()
        }
    }
}

@Composable
private fun ShowInspectorApp() {
    CallInspectorTheme() {
        val diagnosticsViewModel: DiagnosticsViewModel = viewModel() // Hilt-backed because Activity is @AndroidEntryPoint

        Surface(color = MaterialTheme.colorScheme.background) {
            AppNavGraph(diagnosticsViewModel)
        }
    }
}
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CallInspectorTheme {
        ShowInspectorApp()
    }
}