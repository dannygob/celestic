package com.example.celestic.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.celestic.ui.screen.CameraView
import com.example.celestic.ui.screen.DashboardScreen
import com.example.celestic.ui.screen.StatusScreen

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.DASHBOARD
    ) {
        composable(NavRoutes.DASHBOARD) {
            DashboardScreen(navController = navController)
        }
        composable(NavRoutes.CAMERA) {
            CameraView(navController = navController)
        }
        composable(NavRoutes.STATUS) {
            StatusScreen(navController = navController)
        }
    }
}