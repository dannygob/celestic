package com.example.celestic.ui.screen


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.celestic.models.DetectionItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsCountersinkScreen(
    index: Int,
    detectionItems: List<DetectionItem>,
    navController: NavHostController,
) {
    val item = detectionItems.getOrNull(index)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Avellanado") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (item == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No se encontró información para el índice: $index")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "🔵 Detalles del Avellanado Detectado",
                    style = MaterialTheme.typography.titleMedium
                )
                Text("Posición X: ${item.x}")
                Text("Posición Y: ${item.y}")
                Text("Diámetro: ${item.diameter}")
                Text("Etiqueta: ${item.label}")

                Spacer(modifier = Modifier.height(24.dp))

                Button(onClick = { navController.popBackStack() }) {
                    Text("Volver")
                }
            }
        }
    }
}