package com.example.celestic.navigation


import androidx.compose.runtime.Composable
import androidx.compose.ui.UiComposable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.celestic.ui.screen.CalibrationScreen
import com.example.celestic.ui.screen.CameraScreen
import com.example.celestic.ui.screen.DashboardScreen
import com.example.celestic.ui.screen.DetailsScreen
import com.example.celestic.ui.screen.DetectionListScreen
import com.example.celestic.ui.screen.InspectionPreviewScreen
import com.example.celestic.ui.screen.LoginScreen
import com.example.celestic.ui.screen.ReportRequestDialog
import com.example.celestic.ui.screen.ReportsScreen
import com.example.celestic.ui.screen.SettingsScreen
import com.example.celestic.ui.screen.StatusScreen
import com.example.celestic.viewmodel.SharedViewModel

@Composable
@UiComposable
fun NavigationGraph(
    navController: NavHostController,
    sharedViewModel: SharedViewModel
) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(navController, sharedViewModel)
        }
        composable(NavigationRoutes.Dashboard.route) {
            DashboardScreen(navController, sharedViewModel = sharedViewModel)
        }

        composable(NavigationRoutes.Camera.route) {
            CameraScreen(navController)
        }

        composable(
            NavigationRoutes.Details.route,
            arguments = listOf(navArgument("detailType") { type = NavType.StringType })
        ) { backStackEntry ->
            val detailType = backStackEntry.arguments?.getString("detailType") ?: "hole"
            DetailsScreen(navController, detailType, sharedViewModel = sharedViewModel)
        }

        composable(NavigationRoutes.Calibration.route) {
            CalibrationScreen(navController, sharedViewModel = sharedViewModel)
        }

        composable(NavigationRoutes.ReportDialog.route) {
            ReportRequestDialog(
                onDismiss = { navController.popBackStack() },
                onConfirm = { navController.popBackStack() } // lógica real si quieres enviar algo
            )
        }

        composable(NavigationRoutes.Preview.route) {
            InspectionPreviewScreen(navController, sharedViewModel = sharedViewModel)
        }
        composable("settings") {
            SettingsScreen(navController, sharedViewModel = sharedViewModel)
        }
        composable("detection_list") {
            DetectionListScreen(navController, sharedViewModel = sharedViewModel)
        }
        composable(NavigationRoutes.Reports.route) {
            ReportsScreen(navController, sharedViewModel = sharedViewModel)
        }
        composable(NavigationRoutes.Status.route) {
            StatusScreen(navController, sharedViewModel = sharedViewModel)
        }
    }
}