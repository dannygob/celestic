package com.example.celestic.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


/**
 * Dialog shown when an inspection is completed successfully.
 */
@Composable
fun ApprovedResultDialog(
    onNewInspection: () -> Unit,
    onViewReport: () -> Unit,
    onGoToDetail: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("Inspection Completed") },
        text = { Text("The part has been processed. What would you like to do next?") },
        confirmButton = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = onNewInspection,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("New inspection")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onViewReport,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View reports")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onGoToDetail,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Go to technical details")
                }
            }
        },
        dismissButton = {}
    )
}
