package com.example.callinspector.di

import android.content.Context
import androidx.room.Room
import com.example.callinspector.diagnostics.data.local.CallInspectorDatabase
import com.example.callinspector.diagnostics.data.local.dao.HistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CallInspectorDatabase {
        return Room.databaseBuilder(
            context,
            CallInspectorDatabase::class.java,
            "call_inspector_db"
        ).build()
    }

    @Provides
    fun provideHistoryDao(database: CallInspectorDatabase): HistoryDao {
        return database.historyDao()
    }
}