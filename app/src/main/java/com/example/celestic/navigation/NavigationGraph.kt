package com.example.celestic.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.celestic.ui.screen.*

@Composable
fun NavigationGraph() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = NavRoutes.DASHBOARD) {
        composable(NavRoutes.DASHBOARD) {
            DashboardScreen(navController)
        }
        composable(NavRoutes.CAMERA) {
            CameraScreen(navController)
        }
        composable(NavRoutes.STATUS) {
            StatusScreen(navController)
        }
        composable(NavRoutes.DETAILS) {
            val detectionId = it.arguments?.getString("detectionId")
            DetailsScreen(navController, detectionId ?: "")
        }
        composable(NavRoutes.INSPECTION_PREVIEW) {
            InspectionPreviewScreen(navController)
        }
        composable(NavRoutes.CALIBRATION) {
            CalibrationScreen(navController)
        }
    }
}
