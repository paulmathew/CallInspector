package com.example.callinspector.diagnostics.domain.model

data class DeviceHealth(
    val brand: String = "",
    val model: String = "",
    val androidVersion: String = "",
    val coreCount: Int = 0,
    val ramTotalGb: Double = 0.0,
    val ramAvailableGb: Double = 0.0,
    val storageTotalGb: Double = 0.0,
    val storageFreeGb: Double = 0.0,
    val batteryLevel: Int = 0,
    val isCharging: Boolean = false,
    val hasGyroscope: Boolean = false,
    val hasAccelerometer: Boolean = false,
    val hasMagnetometer: Boolean = false
)