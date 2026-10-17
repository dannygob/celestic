package com.example.celestic.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.celestic.viewmodel.DashboardViewModel

@Composable
fun EdgeAdjustDialog(
    blueprintName: String,
    edges: List<DashboardViewModel.EditableEdge>,
    onUpdateEdge: (edgeIndex: Int, nominalMm: Double, toleranceMm: Double) -> Unit,
    onSave: () -> Unit,
    onReset: () -> Unit,
    onAdjustToBlueprint: () -> Unit,
    onDismiss: () -> Unit,
    isDarkMode: Boolean
) {
    val bgColor = if (isDarkMode) Color(0xFF0F172A) else Color.White
    val textColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val cardBorder = if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0)
    val primaryColor = Color(0xFF0284C7)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = primaryColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Architecture,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        "Cotas y Tolerancias de Aristas",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        "Plano: $blueprintName (${edges.size} aristas)",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    color = primaryColor.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            "Cada arista de la pieza no cuadrada se verifica individualmente contra su cota nominal y tolerancia técnica (±0.8 mm) del plano.",
                            fontSize = 11.sp,
                            color = if (isDarkMode) Color(0xFFBAE6FD) else Color(0xFF0369A1),
                            lineHeight = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Medidas oficiales: E1: 135mm, E2: 61mm, E3: 90mm, E4: 40.3mm, E5: 125mm, E6: 46.1mm, E7: 85mm, E8: 25.5mm (Espesor omitido)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF0284C7)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Botón directo para ajustar automáticamente todas las aristas al plano
                Button(
                    onClick = onAdjustToBlueprint,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "AJUSTAR TODAS AL PLANO (E1 - E8)",
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(edges, key = { it.edgeIndex }) { edge ->
                        EdgeRowItem(
                            edge = edge,
                            onUpdate = { nom, tol -> onUpdateEdge(edge.edgeIndex, nom, tol) },
                            isDarkMode = isDarkMode,
                            cardBorder = cardBorder,
                            textColor = textColor
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Guardar Cotas", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TextButton(onClick = onReset) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Restablecer", fontSize = 11.sp)
                }
                TextButton(onClick = onDismiss) {
                    Text("Cerrar", fontSize = 11.sp)
                }
            }
        },
        containerColor = bgColor,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun EdgeRowItem(
    edge: DashboardViewModel.EditableEdge,
    onUpdate: (nominalMm: Double, toleranceMm: Double) -> Unit,
    isDarkMode: Boolean,
    cardBorder: Color,
    textColor: Color
) {
    var nominalText by remember(edge.nominalMm) { mutableStateOf(String.format(java.util.Locale.US, "%.1f", edge.nominalMm)) }
    var tolText by remember(edge.toleranceMm) { mutableStateOf(String.format(java.util.Locale.US, "%.2f", edge.toleranceMm)) }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, cardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF0284C7),
                modifier = Modifier.width(36.dp).height(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        edge.id,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Arista ${edge.edgeIndex}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    "Cota nominal (mm)",
                    fontSize = 9.sp,
                    color = Color.Gray
                )
            }

            // Input Nominal
            OutlinedTextField(
                value = nominalText,
                onValueChange = { input ->
                    nominalText = input
                    val nom = input.toDoubleOrNull()
                    val tol = tolText.toDoubleOrNull() ?: edge.toleranceMm
                    if (nom != null && nom > 0) {
                        onUpdate(nom, tol)
                    }
                },
                modifier = Modifier.width(76.dp).height(48.dp),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(6.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text("±", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)

            Spacer(modifier = Modifier.width(4.dp))

            // Input Tolerance
            OutlinedTextField(
                value = tolText,
                onValueChange = { input ->
                    tolText = input
                    val nom = nominalText.toDoubleOrNull() ?: edge.nominalMm
                    val tol = input.toDoubleOrNull()
                    if (tol != null && tol > 0) {
                        onUpdate(nom, tol)
                    }
                },
                modifier = Modifier.width(62.dp).height(48.dp),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 11.sp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(6.dp)
            )
        }
    }
}
