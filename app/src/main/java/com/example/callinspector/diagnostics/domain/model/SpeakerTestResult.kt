package com.example.callinspector.diagnostics.domain.model

data class SpeakerTestResult(
    val playbackSucceeded: Boolean,
    val currentVolume: Int,
    val maxVolume: Int
){
    val volumeRatio: Float
        get() = if (maxVolume == 0) 0f else currentVolume.toFloat() / maxVolume
}
