package com.example.celestic.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.celestic.models.enums.ReportFormat

/**
 * Dialog that allows the user to select the desired report format.
 * Options include PDF, Word, CSV, and JSON.
 */
@Composable
fun ReportFormatSelectionDialog(
    onDismiss: () -> Unit,
    onFormatSelected: (ReportFormat) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Report Format") },
        text = {
            Column {
                // PDF option
                FormatOptionItem(
                    label = "PDF Document",
                    icon = Icons.Default.Assessment
                ) { onFormatSelected(ReportFormat.PDF) }

                // Word option
                FormatOptionItem(
                    label = "Word Document (DOCX)",
                    icon = Icons.Default.Assessment
                ) { onFormatSelected(ReportFormat.WORD) }

                // CSV option
                FormatOptionItem(
                    label = "Text Data (CSV)",
                    icon = Icons.Default.Assessment
                ) { onFormatSelected(ReportFormat.CSV) }

                // JSON option
                FormatOptionItem(
                    label = "Technical Summary (JSON)",
                    icon = Icons.Default.Assessment
                ) { onFormatSelected(ReportFormat.JSON) }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Single selectable item inside the report format dialog.
 * Displays an icon and a label, and triggers a callback when clicked.
 */
@Composable
fun FormatOptionItem(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Color(0xFF4FC3F7)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 14.sp
        )
    }
}
