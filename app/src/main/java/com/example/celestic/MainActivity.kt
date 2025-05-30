package com.example.celestic

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.celestic.models.DetectionItem
import com.example.celestic.ui.screens.CameraView // Assuming CameraView is in this package
import com.example.celestic.ui.screens.DetailsHoleScreen // Assuming DetailsHoleScreen is in this package
import com.example.celestic.utils.OpenCVInitializer

class MainActivity : ComponentActivity() {

    // private val detectionItems = mutableListOf<DetectionItem>() // Almacena elementos detectados
    // Changed to State for Compose to observe changes
    private var detectionItemsState by mutableStateOf<List<DetectionItem>>(emptyList())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var cameraPermissionGranted by remember { mutableStateOf(false) }
            val navController = rememberNavController()

            val requestPermissionLauncher = remember {
                registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                    cameraPermissionGranted = isGranted
                    if (!isGranted) {
                        Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            LaunchedEffect(Unit) {
                when (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.CAMERA)) {
                    PackageManager.PERMISSION_GRANTED -> {
                        cameraPermissionGranted = true
                    }
                    else -> {
                        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
            }

            if (cameraPermissionGranted) {
                // Definición de las rutas de la aplicación
                NavHost(navController = navController, startDestination = "camera") {
                    composable("camera") {
                        CameraView(
                            navController = navController,
                            detectionItems = detectionItemsState,
                            onDetectionResult = { newDetections ->
                                detectionItemsState = newDetections
                            }
                        )
                    }
                    composable("detailsHole/{index}") { backStackEntry ->
                        val index = backStackEntry.arguments?.getString("index")?.toIntOrNull() ?: 0
                        DetailsHoleScreen(index = index, detectionItems = detectionItemsState, navController = navController)
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Camera permission is required to use this app.")
                    Button(onClick = {
                        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }) {
                        Text("Request Permission")
                    }
                }
            }
        }

        // Verificar y cargar OpenCV
        if (!OpenCVInitializer.initOpenCV(this)) {
            Log.e("OpenCV", "Error initializing OpenCV.")
            Toast.makeText(this, "Error initializing OpenCV", Toast.LENGTH_SHORT).show()
        } else {
            Log.d("OpenCV", "OpenCV initialized successfully.")
        }
    }
}