package com.example.celestic.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.celestic.ui.theme.rememberScreenColors

@Composable
fun PermissionsScreen(onGrantPermissions: () -> Unit) {
    val colors = rememberScreenColors(false) // Usamos modo claro/oscuro por defecto

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            color = colors.accentColor.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(50.dp),
                    tint = colors.accentColor
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Permisos Necesarios",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Para que Celestic funcione correctamente, necesitamos acceso a los siguientes servicios del sistema.",
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        PermissionItem(
            "Cámara",
            "Para la captura y análisis de piezas.",
            Icons.Default.CameraAlt,
            colors.accentColor,
            colors.textColor
        )
        PermissionItem(
            "Ubicación y Bluetooth",
            "Para la vinculación de dispositivos y sincronización.",
            Icons.Default.Settings,
            colors.accentColor,
            colors.textColor
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onGrantPermissions,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.accentColor)
        ) {
            Text("CONCEDER PERMISOS", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Celestic no funcionará si los permisos no son otorgados.",
            fontSize = 12.sp,
            color = colors.errorColor,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PermissionItem(
    title: String,
    description: String,
    icon: ImageVector,
    accentColor: Color,
    textColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, color = textColor)
            Text(description, fontSize = 13.sp, color = Color.Gray)
        }
    }
}
