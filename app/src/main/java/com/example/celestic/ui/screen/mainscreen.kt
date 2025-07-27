package com.example.celestic.ui.screen


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.celestic.viewmodel.MainViewModel

@Composable
fun MainScreen(
    onNavigateToDetail: (String) -> Unit,
    viewModel: MainViewModel = viewModel(),
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Celestic Detector") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = { onNavigateToDetail("HOLE_DETECTION") }) {
                Text("Detectar Agujeros")
            }

            Button(onClick = { onNavigateToDetail("ALODINE_DETECTION") }) {
                Text("Detectar Alodine")
            }

            Button(onClick = { onNavigateToDetail("COUNTERSINK_DETECTION") }) {
                Text("Detectar Avellanados")
            }

            Button(onClick = { onNavigateToDetail("STATUS") }) {
                Text("Ver Estado")
            }
        }
    }
}