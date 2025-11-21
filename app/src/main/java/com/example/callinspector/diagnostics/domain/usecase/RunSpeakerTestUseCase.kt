package com.example.callinspector.diagnostics.domain.usecase

import com.example.callinspector.diagnostics.domain.model.SpeakerTestResult
import kotlinx.coroutines.delay
import javax.inject.Inject

class RunSpeakerTestUseCase @Inject constructor() {
    suspend operator fun invoke(): SpeakerTestResult {

        delay(800)
        return SpeakerTestResult(success = true)
    }
}