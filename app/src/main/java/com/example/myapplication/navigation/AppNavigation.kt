package com.example.celestic.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.celestic.ui.CameraView
import com.example.celestic.ui.DetailsHoleScreen
import com.example.celestic.models.DetectionItem

@Composable
fun AppNavigation(
    navController: NavHostController,
    detectionItems: List<DetectionItem>
) {
    NavHost(navController = navController, startDestination = "camera") {
        // Ruta para la vista de la cámara
        composable("camera") {
            CameraView(navController = navController)
        }

        // Ruta para los detalles del agujero (requiere parámetro index)
        composable("detailsHole/{index}") { backStackEntry ->
            val index = backStackEntry.arguments?.getString("index")?.toIntOrNull() ?: 0
            DetailsHoleScreen(
                index = index,
                detectionItems = detectionItems,
                navController = navController
            )
        }
    }
}