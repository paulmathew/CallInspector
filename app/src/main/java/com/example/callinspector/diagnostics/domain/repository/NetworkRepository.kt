package com.example.callinspector.diagnostics.domain.repository

import com.example.callinspector.diagnostics.domain.model.NetworkHealth
import kotlinx.coroutines.flow.Flow

interface NetworkRepository {
    /**
     * Emits live updates of the network health status.
     * Starts with Pinging -> Downloading -> Complete.
     */
    fun runFullNetworkDiagnostic(): Flow<NetworkHealth>
}