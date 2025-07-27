package com.example.celestic.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.celestic.models.DetectionItem
import com.example.celestic.ui.screen.*

@Composable
fun AppNavigation(
    navController: NavHostController,
    detectionHoleItems: List<DetectionItem>,
    detectionAlodineItems: List<DetectionItem>,
    detectionCountersinkItems: List<DetectionItem>,
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.CAMERA
    ) {
        // 📷 Vista de cámara
        composable(NavRoutes.CAMERA) {
            CameraView(navController = navController)
        }

        // 🕳️ Detalles de agujero
        composable(
            NavRoutes.DETAILS_HOLE,
            arguments = listOf(navArgument("index") { type = NavType.IntType })
        ) { backStackEntry ->
            val index = backStackEntry.arguments?.getInt("index") ?: 0
            DetailsHoleScreen(
                index = index,
                detectionItems = detectionHoleItems,
                navController = navController
            )
        }

        // 🟡 Detalles de Alodine
        composable(
            NavRoutes.DETAILS_ALODINE,
            arguments = listOf(navArgument("index") { type = NavType.IntType })
        ) { backStackEntry ->
            val index = backStackEntry.arguments?.getInt("index") ?: 0
            DetailsAlodineScreen(
                index = index,
                detectionItems = detectionAlodineItems,
                navController = navController
            )
        }

        // 🔵 Detalles de Avellanado
        composable(
            NavRoutes.DETAILS_COUNTERSINK,
            arguments = listOf(navArgument("index") { type = NavType.IntType })
        ) { backStackEntry ->
            val index = backStackEntry.arguments?.getInt("index") ?: 0
            DetailsCountersinkScreen(
                index = index,
                detectionItems = detectionCountersinkItems,
                navController = navController
            )
        }

        // 📊 Estado general
        composable(NavRoutes.STATUS) {
            StatusScreen(navController = navController)
        }
    }
}