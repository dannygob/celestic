package com.example.celestic.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.celestic.models.DetectionItem

@Composable
fun DetailsHoleScreen(
    index: Int, // Índice del agujero seleccionado
    detectionItems: List<DetectionItem>, // Lista de elementos detectados
    navController: NavController
) {
    // Obtener el agujero específico basado en el índice
    val detectionItem = detectionItems.getOrNull(index)

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Detection Details") })
        },
        floatingActionButton = {
            Button(onClick = { navController.popBackStack() }) { // Changed to popBackStack
                Text("Back to Camera")
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp) // Additional padding for content
                .fillMaxSize(),
            horizontalAlignment = Alignment.Start // Align content to the start
        ) {
            if (detectionItem != null) {
                Text(
                    text = "Details for Item #${index + 1}", // Generic for any item type
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        InfoRow(label = "Type:", value = detectionItem.type)
                        detectionItem.position?.let {
                            InfoRow(label = "Position (X,Y):", value = "%.2f, %.2f".format(it.x, it.y))
                        }
                        detectionItem.diameter?.let {
                            InfoRow(label = "Diameter (px):", value = it.toString())
                        }
                        detectionItem.width?.let {
                            InfoRow(label = "Width (px):", value = it.toString())
                        }
                        detectionItem.height?.let {
                            InfoRow(label = "Height (px):", value = it.toString())
                        }
                        // Add more fields if they exist in DetectionItem, e.g., classification
                        // InfoRow(label = "Classification:", value = detectionItem.classification ?: "N/A")
                    }
                }

                Text(
                    text = "Measurement Report",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        InfoRow(label = "Status:", value = "Detected")
                        detectionItem.position?.let {
                            InfoRow(label = "Coordinates (X,Y):", value = "%.2f, %.2f".format(it.x, it.y))
                        }
                        // Based on item type, display relevant measurement
                        when (detectionItem.type) {
                            "agujero", "avellanado", "anodizado" -> { // Assuming these types relate to holes
                                detectionItem.diameter?.let {
                                    InfoRow(label = "Diameter:", value = "$it pixels")
                                }
                                // Example for classification if it were part of DetectionItem
                                // InfoRow(label = "Classification:", value = detectionItem.classification ?: "Standard")
                            }
                            "lamina" -> {
                                detectionItem.width?.let {
                                    InfoRow(label = "Width:", value = "$it pixels")
                                }
                                detectionItem.height?.let {
                                    InfoRow(label = "Height:", value = "$it pixels")
                                }
                            }
                            else -> {
                                Text("No specific measurement report for type: ${detectionItem.type}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

            } else {
                Text(
                    "Item details not found.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), modifier = Modifier.width(150.dp))
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}