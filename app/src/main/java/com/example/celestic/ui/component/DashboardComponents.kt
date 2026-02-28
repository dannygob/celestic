package com.example.celestic.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable

fun PlaceholderCamera(text: String = "Listo para inspección") {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable

fun MainButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text)
    }
}

@Composable

fun DisabledButton(text: String) {
    Button(
        onClick = {},
        enabled = false,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text)
    }
}

@Composable

fun ApprovedResultDialog(
    onNewInspection: () -> Unit,
    onViewReport: () -> Unit,
    onGoToDetail: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("Inspección Finalizada") },
        text = { Text("La pieza ha sido procesada. ¿Qué desea hacer?") },
        confirmButton = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = onNewInspection,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Nueva inspección")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onViewReport,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ver informes")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onGoToDetail,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ir al detalle técnico")
                }
            }
        },
        dismissButton = {}
    )
}