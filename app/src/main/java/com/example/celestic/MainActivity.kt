package com.example.celestic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.celestic.navigation.NavigationGraph
import com.example.celestic.ui.screen.PermissionsScreen
import com.example.celestic.ui.theme.CelesticTheme
import com.example.celestic.utils.OpenCVInitializer
import com.example.celestic.utils.hasRequiredPermissions
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

            var permissionsGranted by remember { mutableStateOf(hasRequiredPermissions(context)) }

            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { results ->
                permissionsGranted = results.values.all { it }
            }

            CelesticTheme(useDarkTheme = isDarkMode) {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding()
                ) {
                    if (permissionsGranted) {
                        val navController = rememberNavController()
                        NavigationGraph(
                            navController = navController,
                            sharedViewModel = sharedViewModel
                        )
                    } else {
                        PermissionsScreen(onGrantPermissions = {
                            val required = mutableListOf(
                                android.Manifest.permission.CAMERA,
                                android.Manifest.permission.ACCESS_FINE_LOCATION
                            )
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                                required.add(android.Manifest.permission.BLUETOOTH_CONNECT)
                                required.add(android.Manifest.permission.BLUETOOTH_SCAN)
                            }
                            permissionLauncher.launch(required.toTypedArray())
                        })
                    }
                }
            }
        }
    }
}