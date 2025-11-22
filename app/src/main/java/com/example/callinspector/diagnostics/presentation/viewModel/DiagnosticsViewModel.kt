package com.example.callinspector.diagnostics.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.callinspector.diagnostics.domain.model.SpeakerTestResult
import com.example.callinspector.diagnostics.domain.usecase.RunAudioTestUseCase
import com.example.callinspector.diagnostics.domain.usecase.RunSpeakerTestUseCase
import com.example.callinspector.utils.loge
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
    val isRunning: Boolean = false,
    val micSuccess: Boolean? = null,
    // Speaker-related
    val speakerSuccess: Boolean? = null,
    val speakerPlaybackSucceeded: Boolean? = null,
    val speakerVolume: Int = 0,
    val speakerMaxVolume: Int = 0,
    val awaitingSpeakerConfirmation: Boolean = false
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
                    speakerPlaybackSucceeded = speakerResult.playbackSucceeded,
                    speakerVolume = speakerResult.currentVolume,
                    speakerMaxVolume = speakerResult.maxVolume,
                    // don’t mark speakerSuccess yet, wait for user input
                    awaitingSpeakerConfirmation = true,
                    isRunning = false,
                    // stay on SpeakerTest step until user answers
                    currentStep = DiagnosticStep.SpeakerTest
                )
            }

        }

    }

    fun reset() {
        _uiState.value = DiagnosticsUiState()
    }
    fun onSpeakerHeard(heard: Boolean) {
        _uiState.update {
            it.copy(
                speakerSuccess = heard,
                awaitingSpeakerConfirmation = false,
                currentStep = DiagnosticStep.Completed
            )
        }
    }

}