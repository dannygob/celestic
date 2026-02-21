package com.example.celestic.ui.component

import androidx.compose.foundation.layout.*
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
                FormatOptionItem("PDF Document", Icons.Default.Assessment) { onFormatSelected(ReportFormat.PDF) }
                FormatOptionItem(
                    "Word Document (DOCX)",
                    Icons.Default.Assessment
                ) { onFormatSelected(ReportFormat.WORD) }
                FormatOptionItem("Text Data (CSV)", Icons.Default.Assessment) { onFormatSelected(ReportFormat.CSV) }
                FormatOptionItem(
                    "Technical Summary (JSON)",
                    Icons.Default.Assessment
                ) { onFormatSelected(ReportFormat.JSON) }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun FormatOptionItem(label: String, icon: ImageVector, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(12.dp)
    ) {
        Icon(icon, null, modifier = Modifier.size(20.dp), tint = Color(0xFF4FC3F7))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, color = Color.White, fontSize = 14.sp)
    }
}
