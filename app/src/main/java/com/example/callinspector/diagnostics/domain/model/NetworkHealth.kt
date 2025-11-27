package com.example.callinspector.diagnostics.domain.model

data class NetworkHealth(
    val latencyMs: Long = 0,
    val downloadSpeedMbps: Double = 0.0,
    val jitterMs: Long = 0,
    val packetLossPercent: Int = 0,
    val stage: TestStage = TestStage.IDLE
)

enum class TestStage {
    IDLE, PINGING, DOWNLOADING, CALCULATING, COMPLETE, ERROR
}