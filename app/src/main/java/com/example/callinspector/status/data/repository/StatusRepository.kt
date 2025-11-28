package com.example.callinspector.status.data.repository

import com.example.callinspector.status.data.api.StatusApiService
import com.example.callinspector.status.data.model.ServiceStatus
import javax.inject.Inject

class StatusRepository @Inject constructor(
    private val api: StatusApiService
) {
    suspend fun getStatuses(): List<ServiceStatus> {
        return try {
            api.getServiceStatuses().services
        } catch (e: Exception) {
            generateMockData()
        }
    }

    private fun generateMockData(): List<ServiceStatus> {
        return listOf(
            ServiceStatus("1", "Zoom Video", "operational", 45, "Just now"),
            ServiceStatus("2", "Microsoft Teams", "degraded", 120, "5 mins ago"),
            ServiceStatus("3", "Google Meet", "operational", 38, "Just now"),
            ServiceStatus("4", "Slack Huddles", "outage", 0, "1 hour ago"),
            ServiceStatus("5", "Discord Voice", "operational", 22, "Just now")
        )
    }
}