package com.example.celestic.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.celestic.ui.component.ReportRequestDialog
import com.example.celestic.ui.screen.CalibrationScreen
import com.example.celestic.ui.screen.DashboardScreen
import com.example.celestic.ui.screen.DetailsScreen
import com.example.celestic.ui.screen.DetectionListScreen
import com.example.celestic.ui.screen.LoginScreen
import com.example.celestic.ui.screen.ReportsScreen
import com.example.celestic.ui.screen.SettingsScreen
import com.example.celestic.ui.screen.StatusScreen
import com.example.celestic.viewmodel.SharedViewModel

/**
 * Defines the navigation graph for the application using Jetpack Compose Navigation.
 *
 * Each composable destination corresponds to a screen in the app.
 * NavigationRoutes is used to centralize route definitions.
 */
@Composable
fun NavigationGraph(
    navController: NavHostController,
    sharedViewModel: SharedViewModel
) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        // Login screen
        composable("login") {
            LoginScreen(navController, sharedViewModel)
        }

        // Dashboard
        composable(NavigationRoutes.Dashboard.route) {
            DashboardScreen(navController, sharedViewModel = sharedViewModel)
        }

        // Details screen with arguments
        composable(
            NavigationRoutes.Details.route,
            arguments = listOf(
                navArgument("detailType") { type = NavType.StringType },
                navArgument("id") { type = NavType.StringType; nullable = true }
            )
        ) { backStackEntry ->
            val detailType = backStackEntry.arguments?.getString("detailType") ?: "hole"
            val id = backStackEntry.arguments?.getString("id")?.toLongOrNull()

            DetailsScreen(
                navController = navController,
                detailType = detailType,
                detectionId = id,
                sharedViewModel = sharedViewModel
            )
        }

        // Calibration
        composable(NavigationRoutes.Calibration.route) {
            CalibrationScreen(navController, sharedViewModel = sharedViewModel)
        }

        // Report request dialog
        composable(NavigationRoutes.ReportDialog.route) {
            ReportRequestDialog(
                onDismiss = { navController.popBackStack() },
                onConfirm = { navController.popBackStack() }
            )
        }

        // Settings
        composable("settings") {
            SettingsScreen(navController, sharedViewModel = sharedViewModel)
        }

        // Reports
        composable(NavigationRoutes.Reports.route) {
            ReportsScreen(navController, sharedViewModel = sharedViewModel)
        }

        // Detection list
        composable("detection_list") {
            DetectionListScreen(navController, sharedViewModel = sharedViewModel)
        }

        // Status screen
        composable(NavigationRoutes.Status.route) {
            StatusScreen(navController, sharedViewModel = sharedViewModel)
        }
    }
}
