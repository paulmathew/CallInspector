package com.example.callinspector.diagnostics.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.callinspector.diagnostics.data.local.dao.HistoryDao
import com.example.callinspector.diagnostics.data.local.entity.DiagnosticReportEntity

@Database(entities = [DiagnosticReportEntity::class], version = 1)
abstract class CallInspectorDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
}