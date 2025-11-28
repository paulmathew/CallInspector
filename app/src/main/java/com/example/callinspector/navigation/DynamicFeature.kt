package com.example.callinspector.navigation

import androidx.compose.runtime.Composable

interface DynamicFeature {
    @Composable
    fun Content(onBack: () -> Unit)
}