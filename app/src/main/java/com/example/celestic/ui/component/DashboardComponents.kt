package com.example.celestic.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.unit.dp

@Composable
@UiComposable
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
@UiComposable
fun MainButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text)
    }
}

@Composable
@UiComposable
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
@UiComposable
fun ApprovedResultDialog(
    onNewInspection: () -> Unit,
    onViewReport: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("Inspección aprobada") },
        text = { Text("¿Qué deseas hacer ahora?") },
        confirmButton = {
            Button(onClick = onNewInspection) {
                Text("Nueva inspección")
            }
        },
        dismissButton = {
            Button(onClick = onViewReport) {
                Text("Ver reporte")
            }
        }
    )
}