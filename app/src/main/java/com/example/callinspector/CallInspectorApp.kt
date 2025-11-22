package com.example.callinspector

import android.app.Application
import android.content.Context
import com.example.callinspector.diagnostics.domain.usecase.RunSpeakerTestUseCase
import dagger.Provides
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.qualifiers.ApplicationContext

@HiltAndroidApp
class CallInspectorApp : Application() {
}