package com.example.celestic.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.celestic.R
import com.example.celestic.models.DetectionItem
import com.example.celestic.models.enums.DetectionStatus
import com.example.celestic.ui.theme.rememberScreenColors
import java.util.Locale

/**
 * Tarjeta de inspección de alta fidelidad con tipografía JetBrains Mono para cotas
 * y contraste nítido en modo claro y oscuro.
 */
@Composable
fun DetectionItemCard(
    item: DetectionItem,
    isDarkMode: Boolean = true,
    onClick: () -> Unit = {}
) {
    val locale = Locale.getDefault()
    val colors = rememberScreenColors(isDarkMode)

    val (statusColor, statusBg) = when (item.status) {
        DetectionStatus.OK -> Color(0xFF10B981) to Color(0xFF10B981).copy(alpha = 0.15f)
        DetectionStatus.WARNING -> Color(0xFFF59E0B) to Color(0xFFF59E0B).copy(alpha = 0.15f)
        DetectionStatus.NOT_ACCEPTED -> Color(0xFFEF4444) to Color(0xFFEF4444).copy(alpha = 0.15f)
    }

    val statusIcon = when (item.status) {
        DetectionStatus.OK -> Icons.Default.CheckCircle
        DetectionStatus.WARNING -> Icons.Default.Warning
        DetectionStatus.NOT_ACCEPTED -> Icons.Default.Error
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
        border = BorderStroke(1.dp, colors.cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status icon badge
            Surface(
                shape = CircleShape,
                color = statusBg,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Main info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${item.type} • #${item.id}",
                    color = colors.textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = stringResource(R.string.batch_label, item.linkedQrCode ?: "N/A"),
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Measurement + status badge
            Column(horizontalAlignment = Alignment.End) {
                item.measurementMm?.let {
                    Text(
                        text = "${String.format(locale, "%.2f", it)} mm",
                        color = colors.textColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        letterSpacing = 0.5.sp
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusBg,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = item.status.name,
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
