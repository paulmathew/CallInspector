package com.example.callinspector.di

import android.content.Context
import com.example.callinspector.diagnostics.domain.usecase.RunSpeakerTestUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    fun provideSpeakerTestUseCase(@ApplicationContext context: Context): RunSpeakerTestUseCase =
        RunSpeakerTestUseCase(context = context)
}