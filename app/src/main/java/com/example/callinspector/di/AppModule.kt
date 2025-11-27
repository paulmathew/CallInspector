package com.example.callinspector.di

import android.content.Context
import com.example.callinspector.diagnostics.data.repository.RealDeviceRepository
import com.example.callinspector.diagnostics.domain.repository.DeviceRepository
import com.example.callinspector.diagnostics.domain.repository.NetworkRepository
import com.example.callinspector.diagnostics.domain.repository.RealNetworkRepository
import com.example.callinspector.diagnostics.domain.usecase.RunSpeakerTestUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    fun provideSpeakerTestUseCase(@ApplicationContext context: Context): RunSpeakerTestUseCase =
        RunSpeakerTestUseCase(context = context)

    @Provides
    @Singleton // Use Singleton for repositories
    fun provideNetworkRepository(): NetworkRepository {
        return RealNetworkRepository()
    }

    @Provides
    @Singleton
    fun provideDeviceRepository(@ApplicationContext context: Context): DeviceRepository {
        return RealDeviceRepository(context)
    }
}