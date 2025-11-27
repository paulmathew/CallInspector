package com.example.callinspector.diagnostics.domain.usecase

import com.example.callinspector.diagnostics.domain.model.NetworkHealth
import com.example.callinspector.diagnostics.domain.repository.NetworkRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RunNetworkTestUseCase @Inject constructor(
    private val repository: NetworkRepository
) {
    operator fun invoke(): Flow<NetworkHealth> {
        return repository.runFullNetworkDiagnostic()
    }
}