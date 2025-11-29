package com.example.callinspector.diagnostics.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diagnostic_reports")
data class DiagnosticReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val finalScore: Int,
    val finalGrade: String,
    // Store simple boolean results
    val micPassed: Boolean,
    val speakerPassed: Boolean,
    val networkPassed: Boolean,
    val cameraPassed: Boolean,
    // Network specifics
    val downloadSpeedMbps: Double,
    val latencyMs: Long
)