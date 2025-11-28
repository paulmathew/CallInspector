package com.example.callinspector.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.callinspector.diagnostics.presentation.ui.DiagnosticsHomeScreen
import com.example.callinspector.diagnostics.presentation.ui.DiagnosticsResultScreen
import com.example.callinspector.diagnostics.presentation.ui.DiagnosticsRunScreen
import com.example.callinspector.diagnostics.presentation.viewModel.DiagnosticsViewModel


enum class RootRoute(val route: String) {
    HOME("home"),
    RUN("run"),
    RESULT("result"),
    STATUS("status") // for dynamic Feature
}

@Composable
fun AppNavGraph(diagnosticsViewModel: DiagnosticsViewModel) {

    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = RootRoute.HOME.route,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(RootRoute.HOME.route) {
            DiagnosticsHomeScreen(
                onStartClick = {
                    diagnosticsViewModel.reset()          // 👈 reset state
                    navController.navigate(RootRoute.RUN.route)
                },
                onStatusClick = {
                    navController.navigate(RootRoute.STATUS.route)
                }
            )
        }
        composable(RootRoute.RUN.route) {
            DiagnosticsRunScreen(
                onGoToResult = {
                    navController.navigate(RootRoute.RESULT.route)
                },
                onBackToHome = {
                    navController.popBackStack(
                        route = RootRoute.HOME.route,
                        inclusive = false
                    )
                },
                viewModel = diagnosticsViewModel

            )

        }
        composable(RootRoute.RESULT.route) {
            DiagnosticsResultScreen(
                onBackToHome = {
                    navController.popBackStack(
                        route = RootRoute.HOME.route,
                        inclusive = false
                    )
                },
                viewModel = diagnosticsViewModel

            )
        }
        composable(RootRoute.STATUS.route) {
                // REFLECTION: Load the class by name
                val clazz = Class.forName("com.example.callinspector.status.StatusEntryPoint")
                val feature = clazz.getDeclaredConstructor().newInstance() as DynamicFeature
                // Render the content
                feature.Content(onBack = { navController.popBackStack() })

        }
    }
}