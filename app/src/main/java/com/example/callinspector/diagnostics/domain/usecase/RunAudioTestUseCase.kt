package com.example.callinspector.diagnostics.domain.usecase

import com.example.callinspector.diagnostics.domain.model.AudioTestResult
import kotlinx.coroutines.delay
import javax.inject.Inject

class RunAudioTestUseCase @Inject constructor() {
    suspend operator fun invoke(): AudioTestResult {
        delay(800)
        return AudioTestResult(success = true, averageAmplitude = 0.7)
    }
}