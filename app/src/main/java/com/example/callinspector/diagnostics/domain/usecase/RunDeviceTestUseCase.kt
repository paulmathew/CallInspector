package com.example.callinspector.diagnostics.domain.usecase

import com.example.callinspector.diagnostics.domain.model.DeviceHealth
import com.example.callinspector.diagnostics.domain.repository.DeviceRepository
import javax.inject.Inject

class RunDeviceTestUseCase @Inject constructor(
    private val repository: DeviceRepository
) {
    suspend operator fun invoke(): DeviceHealth {
        return repository.getDeviceDetails()
    }
}