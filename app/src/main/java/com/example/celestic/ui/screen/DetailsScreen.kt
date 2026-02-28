package com.example.celestic.ui.screen

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.celestic.R
import com.example.celestic.models.enums.DetectionStatus
import com.example.celestic.ui.component.BlueprintView
import com.example.celestic.ui.theme.CelesticTheme
import com.example.celestic.ui.theme.rememberScreenColors
import com.example.celestic.viewmodel.DetailsViewModel
import com.example.celestic.viewmodel.SharedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@UiComposable
fun DetailsScreen(
    navController: NavController,
    detailType: String,
    detectionId: Long? = null,
    detailsViewModel: DetailsViewModel = hiltViewModel(),
    sharedViewModel: SharedViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isDarkMode by sharedViewModel.isDarkMode.collectAsState()
    val colors = rememberScreenColors(isDarkMode)

    // Observar estados del ViewModel
    val detectionItem by detailsViewModel.detectionItem.collectAsState()
    val traceabilityResult by detailsViewModel.traceabilityItem.collectAsState()
    val features = detailsViewModel.features.collectAsState().value ?: emptyList()
    val useInches = sharedViewModel.useInches.collectAsState().value

    var imageBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    LaunchedEffect(detectionItem?.frameId) {
        detectionItem?.frameId?.let { frameId ->
            val file = java.io.File(context.filesDir, "detection_images/$frameId.jpg")
            if (file.exists()) {
                imageBitmap = android.graphics.BitmapFactory.decodeFile(file.absolutePath)
            }
        }
    }

    val cardBg =
        if (isDarkMode) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)

    val reportSentMsg = stringResource(R.string.reportIssueSent)
    var selectedReportFormat by remember { mutableStateOf("PDF") }
    val reportFormats = listOf("PDF", "Word", "Excel", "CSV")

    val title = when (detailType) {
        "hole" -> stringResource(R.string.detailsHole)
        "alodine" -> stringResource(R.string.detailsAlodine)
        "countersink" -> stringResource(R.string.detailsCountersink)
        else -> stringResource(R.string.detailedAnalysis)
    }

    LaunchedEffect(detectionId) {
        detectionId?.let {
            detailsViewModel.loadDetectionById(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = colors.textColor
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            stringResource(R.string.back),
                            tint = colors.textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.topBarBg,
                    titleContentColor = colors.textColor
                )
            )
        },
        containerColor = colors.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sección de Estado de Inspección
            item {
                detectionItem?.let { item ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when (item.status) {
                                DetectionStatus.OK -> Color(0xFF2E7D32).copy(alpha = 0.15f)
                                DetectionStatus.NOT_ACCEPTED -> Color(0xFFC62828).copy(alpha = 0.15f)
                                else -> colors.accentColor.copy(alpha = 0.15f)
                            }
                        ),
                        border = BorderStroke(
                            2.dp,
                            when (item.status) {
                                DetectionStatus.OK -> Color.Green
                                DetectionStatus.NOT_ACCEPTED -> Color.Red
                                else -> colors.accentColor
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (item.status == DetectionStatus.OK) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = null,
                                tint = if (item.status == DetectionStatus.OK) Color.Green else Color.Red,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = if (item.status == DetectionStatus.OK) "INSPECCIÓN APROBADA" else "INSPECCIÓN RECHAZADA",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = colors.textColor
                                )
                                Text(
                                    text = "Batch ID: ${item.frameId}",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
            // Sección de Visualización Técnica
            item {
                SectionHeader(stringResource(R.string.visualAnalysis), colors.accentColor)
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isDarkMode) Color.Black else Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    border = if (!isDarkMode) BorderStroke(1.dp, Color.LightGray) else null
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        imageBitmap?.let { bitmap ->
                            androidx.compose.foundation.Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Captura Real",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit,
                                alpha = 0.4f // Fondo sutil si ponemos el blueprint encima
                            )
                        }
                        BlueprintView(features = features, useInches = useInches)
                    }
                }
            }

            // Sección de Características Detectadas
            item {
                SectionHeader(stringResource(R.string.detectionsHeadline), colors.accentColor)
            }

            items(features) { feature ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = colors.accentColor)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                feature.featureType.uppercase(),
                                color = colors.textColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                "Coord: (${feature.xCoord}, ${feature.yCoord})",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Sección de Trazabilidad
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(stringResource(R.string.traceabilityHeadline), colors.accentColor)
                Spacer(modifier = Modifier.height(8.dp))

                AnimatedContent(
                    targetState = traceabilityResult,
                    label = "traceability"
                ) { result ->
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        when (result) {
                            is com.example.celestic.utils.Result.Success -> {
                                val data = result.data
                                if (data != null) {
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isDarkMode) Color(
                                                0xFF1B263B
                                            ) else Color(0xFFD1D9E6)
                                        )
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            TraceabilityRow(
                                                stringResource(R.string.partCode),
                                                data.code,
                                                colors.textColor,
                                                Color.Gray
                                            )
                                            TraceabilityRow(
                                                stringResource(R.string.modelType),
                                                data.partName,
                                                colors.textColor,
                                                Color.Gray
                                            )
                                            TraceabilityRow(
                                                stringResource(R.string.operator),
                                                data.operatorName,
                                                colors.textColor,
                                                Color.Gray
                                            )
                                            TraceabilityRow(
                                                stringResource(R.string.inspectionDate),
                                                data.inspectionDate,
                                                colors.textColor,
                                                Color.Gray
                                            )
                                            TraceabilityRow(
                                                stringResource(R.string.finalStatus),
                                                data.finalStatus,
                                                colors.textColor,
                                                Color.Gray,
                                                isStatus = true
                                            )
                                        }
                                    }
                                }
                            }

                            is com.example.celestic.utils.Result.Loading -> {
                                CircularProgressIndicator(color = colors.accentColor)
                            }

                            else -> {
                                Text(
                                    stringResource(R.string.noTraceability),
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            // Sección de Exportación de Reporte
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader("EXPORTAR INFORME", colors.accentColor)
                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Seleccione el formato industrial:",
                            color = colors.textColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Fila de Filtros/Formatos
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            reportFormats.forEach { format ->
                                FilterChip(
                                    selected = selectedReportFormat == format,
                                    onClick = { selectedReportFormat = format },
                                    label = { Text(format) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = colors.accentColor,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val detections = detectionItem?.let { listOf(it) } ?: emptyList()
                                val loteId = detectionItem?.frameId ?: "DESCONOCIDO"

                                val file = when (selectedReportFormat) {
                                    "PDF" -> com.example.celestic.utils.generatePdfFromDetections(
                                        context,
                                        detections,
                                        loteId
                                    )

                                    "Word" -> com.example.celestic.utils.generateWordFromDetections(
                                        context,
                                        detections,
                                        loteId
                                    )

                                    "Excel" -> com.example.celestic.utils.generateExcelFromDetections(
                                        context,
                                        detections,
                                        loteId
                                    )

                                    "CSV" -> com.example.celestic.utils.generateCsvFromDetections(
                                        context,
                                        detections,
                                        loteId
                                    )

                                    else -> null
                                }

                                if (file != null && file.exists()) {
                                    Toast.makeText(context, "Informe guardado en: ${file.name}", Toast.LENGTH_LONG)
                                        .show()
                                } else {
                                    Toast.makeText(context, "Error al generar informe", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.accentColor)
                        ) {
                            Text("DESCARGAR INFORME", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Botón de Reporte de Problema
            item {
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedButton(
                    onClick = {
                        Toast.makeText(context, reportSentMsg, Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (isDarkMode) Color.Red.copy(alpha = 0.5f) else Color.Red),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                ) {
                    Icon(Icons.Default.ReportProblem, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("REPORTAR ANOMALÍA EN INSPECCIÓN")
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun TraceabilityRow(
    label: String,
    value: String,
    textColor: Color,
    labelColor: Color,
    isStatus: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = labelColor, fontSize = 14.sp)
        Text(
            value,
            color = if (isStatus && value.contains(
                    "OK",
                    true
                )
            ) Color.Green else if (isStatus && value.contains("NO", true)) Color.Red else textColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun SectionHeader(title: String, color: Color) {
    Text(
        text = title,
        color = color,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
    )
}

@Preview(showBackground = true, locale = "en")
@Preview(showBackground = true, locale = "es")
@Preview(showBackground = true, locale = "zh")
@Composable
fun DetailsScreenPreview() {
    CelesticTheme {
        DetailsScreen(rememberNavController(), "hole")
    }
}