package com.example.celestic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.celestic.navigation.NavigationGraph
import com.example.celestic.ui.theme.CelesticTheme
import com.example.celestic.utils.OpenCVInitializer
import com.example.celestic.utils.hasCameraPermission
import com.example.celestic.viewmodel.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        OpenCVInitializer.initOpenCV(this)


        setContent {
            val sharedViewModel: SharedViewModel = hiltViewModel()
            val isDarkMode by sharedViewModel.isDarkMode.collectAsState()
            val context = LocalContext.current

            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (!isGranted) {
                    // Aquí se podría manejar el caso de permiso denegado
                }
            }

            LaunchedEffect(Unit) {
                if (!hasCameraPermission(context)) {
                    permissionLauncher.launch(android.Manifest.permission.CAMERA)
                }
            }

            CelesticTheme(useDarkTheme = isDarkMode) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    NavigationGraph(
                        navController = navController,
                        sharedViewModel = sharedViewModel
                    )
                }
            }
        }
    }
}