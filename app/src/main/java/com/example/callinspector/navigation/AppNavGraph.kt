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


enum class RootRoute(val route: String) {
    HOME("home"),
    RUN("run"),
    RESULT("result")
}

@Composable
fun AppNavGraph() {

    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = RootRoute.HOME.route,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(RootRoute.HOME.route) {
            DiagnosticsHomeScreen(
                onStartClick = {
                    navController.navigate(RootRoute.RUN.route)
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
                }
            )

        }
        composable(RootRoute.RESULT.route) {
            DiagnosticsResultScreen(
                onBackToHome = {
                    navController.popBackStack(
                        route = RootRoute.HOME.route,
                        inclusive = false
                    )
                }
            )
        }
    }
}