package com.example.callinspector.status

import androidx.compose.runtime.Composable
import androidx.annotation.Keep
import com.example.callinspector.navigation.DynamicFeature
import com.example.callinspector.status.presentation.StatusScreen

// @Keep ensures ProGuard/R8 doesn't rename this class,
// so we can find it via Reflection string.
@Keep
class StatusEntryPoint : DynamicFeature {

    @Composable
    override fun Content(onBack: () -> Unit) {
        StatusScreen(onBackClick = onBack)
    }
}