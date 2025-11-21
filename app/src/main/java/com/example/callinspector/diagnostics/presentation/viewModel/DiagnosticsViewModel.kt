package com.example.callinspector.diagnostics.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.callinspector.diagnostics.domain.usecase.RunAudioTestUseCase
import com.example.callinspector.diagnostics.domain.usecase.RunSpeakerTestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

//-- STEP STATE---
sealed class DiagnosticStep {
    data object Idle : DiagnosticStep()
    data object MicTest : DiagnosticStep()
    data object SpeakerTest : DiagnosticStep()
    data object NetworkTest : DiagnosticStep()
    data object CameraTest : DiagnosticStep()
    data object DeviceTest : DiagnosticStep()
    data object Completed : DiagnosticStep()

}

data class DiagnosticsUiState(
    val currentStep: DiagnosticStep = DiagnosticStep.Idle,
    val isRunning: Boolean = false
)

@HiltViewModel
class DiagnosticsViewModel @Inject constructor(
    private val runAudioTestUseCase: RunAudioTestUseCase,
    private val runSpeakerTestUseCase: RunSpeakerTestUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(DiagnosticsUiState())
    val uiState: StateFlow<DiagnosticsUiState> = _uiState.asStateFlow()


    fun startDiagnostics() {
        // from Idle -> MicTest; ignore if already running

        viewModelScope.launch {
            val state = _uiState.value
            if (state.isRunning) return@launch

            _uiState.update { state ->
                state.copy(
                    isRunning = true,
                    currentStep = DiagnosticStep.MicTest
                )
            }
            val audioResult = runAudioTestUseCase()

            _uiState.update {
                it.copy(
                    currentStep = DiagnosticStep.SpeakerTest
                )
            }
            val speakerResult = runSpeakerTestUseCase()

            _uiState.update {
                it.copy(
                    currentStep = DiagnosticStep.Completed,
                    isRunning = false
                )
            }

        }

    }

//    fun goToNextStep() {
//        viewModelScope.launch {
//            val next = when (_uiState.value.currentStep) {
//                DiagnosticStep.Idle -> DiagnosticStep.MicTest
//                DiagnosticStep.MicTest -> DiagnosticStep.SpeakerTest
//                DiagnosticStep.SpeakerTest -> DiagnosticStep.NetworkTest
//                DiagnosticStep.NetworkTest -> DiagnosticStep.CameraTest
//                DiagnosticStep.CameraTest -> DiagnosticStep.DeviceTest
//                DiagnosticStep.DeviceTest -> DiagnosticStep.Completed
//                DiagnosticStep.Completed -> DiagnosticStep.Completed
//            }
//
//            _uiState.update {
//                val running = next != DiagnosticStep.Completed
//                it.copy(
//                    currentStep = next,
//                    isRunning = running
//                )
//            }
//        }
//    }

    fun reset() {
        _uiState.value = DiagnosticsUiState()
    }

}