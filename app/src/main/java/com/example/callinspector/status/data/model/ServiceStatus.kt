package com.example.callinspector.status.data.model

import com.google.gson.annotations.SerializedName

data class ServiceStatusResponse(
    @SerializedName("services") val services: List<ServiceStatus>
)

data class ServiceStatus(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("status") val status: String, // "operational", "outage", "degraded"
    @SerializedName("latency_ms") val latency: Int,
    @SerializedName("last_updated") val lastUpdated: String
)