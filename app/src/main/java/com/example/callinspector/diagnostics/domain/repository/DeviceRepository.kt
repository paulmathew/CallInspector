package com.example.callinspector.diagnostics.domain.repository

import com.example.callinspector.diagnostics.domain.model.DeviceHealth

interface DeviceRepository {
    suspend fun getDeviceDetails(): DeviceHealth
}