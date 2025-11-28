package com.example.callinspector.diagnostics.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.callinspector.diagnostics.domain.model.SpeakerTestResult
import com.example.callinspector.diagnostics.domain.model.TestStage
import com.example.callinspector.diagnostics.domain.usecase.RunAudioTestUseCase
import com.example.callinspector.diagnostics.domain.usecase.RunDeviceTestUseCase
import com.example.callinspector.diagnostics.domain.usecase.RunNetworkTestUseCase
import com.example.callinspector.diagnostics.domain.usecase.RunSpeakerTestUseCase
import com.example.callinspector.utils.loge
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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
    // CHANGED: Split generic "CameraTest" into two specific steps
    data object BackCameraTest : DiagnosticStep()
    data object FrontCameraTest : DiagnosticStep()
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
    val awaitingSpeakerConfirmation: Boolean = false,

    // Network-related
    val networkSuccess: Boolean? = null,
    val networkLatencyMs: Long? = null,
    val networkJitterMs: Long? = null,
    val networkDownloadKbps: Int? = null,
    val networkPacketLossPercent: Int? = null,

    // for Camera-related
    // CHANGED: Separate results for each camera
    val backCameraSuccess: Boolean? = null,
    val frontCameraSuccess: Boolean? = null,

    // Device health-related
    val deviceHealth: com.example.callinspector.diagnostics.domain.model.DeviceHealth? = null,

    val finalScore: Int = 0,
    val finalGrade: String = "F"
)

@HiltViewModel
class DiagnosticsViewModel @Inject constructor(
    private val runAudioTestUseCase: RunAudioTestUseCase,
    private val runSpeakerTestUseCase: RunSpeakerTestUseCase,
    private val runNetworkTestUseCase: RunNetworkTestUseCase,
    private val runDeviceTestUseCase: RunDeviceTestUseCase
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
                    micSuccess = audioResult.success,
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
                isRunning=true,
                speakerSuccess = heard,
                awaitingSpeakerConfirmation = false,
                currentStep = DiagnosticStep.NetworkTest
            )
        }
        _uiState.update {
            it.copy(
                isRunning = true,
                currentStep = DiagnosticStep.NetworkTest
            )
        }
        // 2. Start Network Test Stream
        viewModelScope.launch {
            runNetworkTestUseCase() // Returns Flow<NetworkHealth>
                .collect { health ->
                    _uiState.update { state ->
                        state.copy(
                            networkLatencyMs = health.latencyMs,
                            networkJitterMs = health.jitterMs,
                            networkDownloadKbps = (health.downloadSpeedMbps * 1000).toInt(),
                            networkPacketLossPercent = health.packetLossPercent,
                            networkSuccess = if (health.stage == TestStage.COMPLETE) true else null,
                            isRunning = health.stage != TestStage.COMPLETE
                        )
                    }

                    if (health.stage == TestStage.COMPLETE) {
                        delay(500)
                        _uiState.update { it.copy(currentStep = DiagnosticStep.BackCameraTest,isRunning=true) }
                    }
                }
        }
    }
    // NEW: Handle Back Camera Result -> Go to Front Camera
    fun onBackCameraResult(success: Boolean) {
        _uiState.update {
            it.copy(
                isRunning=true,
                backCameraSuccess = success,
                currentStep = DiagnosticStep.FrontCameraTest
            )
        }
    }

    // NEW: Handle Front Camera Result -> Finish
    fun onFrontCameraResult(success: Boolean) {
        _uiState.update {
            it.copy(
                frontCameraSuccess = success
            )
        }
        runDeviceDiagnostics()
    }
    fun runDeviceDiagnostics() {
        // Switch step immediately so UI shows "Scanning..."
        _uiState.update { it.copy(currentStep = DiagnosticStep.DeviceTest,isRunning = true) }

        viewModelScope.launch {
            delay(1500)
            val health = runDeviceTestUseCase()
            _uiState.update {
                it.copy(
                    isRunning = false,
                    deviceHealth = health,
                    currentStep = DiagnosticStep.DeviceTest
                )
            }
        }
    }

    //  Logic to calculate the grade
    private fun calculateFinalScore() {
        var score = 100
        val state = _uiState.value

        // Deductions
        if (state.micSuccess != true) score -= 15
        if (state.speakerSuccess != true) score -= 15

        // Camera deductions
        if (state.backCameraSuccess != true) score -= 15
        if (state.frontCameraSuccess != true) score -= 15

        // Network: If loss > 2% or speed < 5Mbps, deduct
        val loss = state.networkPacketLossPercent ?: 0
        val speed = (state.networkDownloadKbps ?: 0) / 1000.0

        // Network deductions (Max -10)
        if (state.networkSuccess != true || loss > 2 || speed < 5.0) {
            score -= 10
        }

        // Device Health (Battery check)
        // Note: Safe call ?. because deviceHealth might be null if test didn't run
        val battery = state.deviceHealth?.batteryLevel ?: 0
        val isCharging = state.deviceHealth?.isCharging == true

        if (battery < 20 && !isCharging) score -= 5

        // Cap at 0
        score = score.coerceAtLeast(0)

        // Calculate Grade
        val grade = when {
            score >= 90 -> "A+"
            score >= 80 -> "A"
            score >= 70 -> "B"
            score >= 50 -> "C"
            else -> "D"
        }

        _uiState.update { it.copy(finalScore = score, finalGrade = grade) }
    }

    //function called by UI (and Test)
    fun finishDiagnostics() {
        calculateFinalScore()
        _uiState.update { it.copy(currentStep = DiagnosticStep.Completed) }
    }

}