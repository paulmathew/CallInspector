package com.example.callinspector.history.data.repository


import com.example.callinspector.diagnostics.data.local.dao.HistoryDao
import com.example.callinspector.diagnostics.data.local.entity.DiagnosticReportEntity
import javax.inject.Inject

class HistoryRepository @Inject constructor(
    private val dao: HistoryDao
) {
    suspend fun saveReport(
        score: Int,
        grade: String,
        mic: Boolean,
        speaker: Boolean,
        net: Boolean,
        cam: Boolean,
        speed: Double,
        latency: Long
    ) {
        val entity = DiagnosticReportEntity(
            timestamp = System.currentTimeMillis(),
            finalScore = score,
            finalGrade = grade,
            micPassed = mic,
            speakerPassed = speaker,
            networkPassed = net,
            cameraPassed = cam,
            downloadSpeedMbps = speed,
            latencyMs = latency
        )
        dao.insertReport(entity)
    }


     fun getHistory() = dao.getAllReports()
}