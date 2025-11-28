package com.example.callinspector.status.data.api

import com.example.callinspector.status.data.model.ServiceStatusResponse
import retrofit2.http.GET

interface StatusApiService {
    @GET("/status")
    suspend fun getServiceStatuses(): ServiceStatusResponse
}