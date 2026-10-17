package com.example.celestic.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.celestic.R
import com.example.celestic.models.enums.Orientation
import com.example.celestic.ui.component.CameraCaptureController
import com.example.celestic.ui.component.CameraPreview
import com.example.celestic.ui.theme.rememberScreenColors
import com.example.celestic.viewmodel.GoldenSampleViewModel
import com.example.celestic.viewmodel.SharedViewModel

/**
 * Pantalla de Configuración y Aprendizaje de Pieza Patrón / Oro (Golden Sample).
 * Permite capturar e integrar TODAS las dimensiones geométricas de la lámina:
 * - Cara ANVERSO (avellanados, márgenes, taladros, cotas exteriores).
 * - Cara REVERSO (espesor de vernier, taladros pasantes, planitud).
 * - Medición calibrada con Vernier (2.0 mm).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoldenSampleScreen(
    navController: NavController,
    viewModel: GoldenSampleViewModel = hiltViewModel(),
    sharedViewModel: SharedViewModel = hiltViewModel()
) {
    val blueprintName by viewModel.blueprintName.collectAsState()
    val hasTwoFaces by viewModel.hasTwoFaces.collectAsState()
    val vernierThickness by viewModel.vernierThicknessMm.collectAsState()
    val obverseFeatures by viewModel.obverseFeatures.collectAsState()
    val reverseFeatures by viewModel.reverseFeatures.collectAsState()
    val obverseSummary by viewModel.obverseDimensionsSummary.collectAsState()
    val reverseSummary by viewModel.reverseDimensionsSummary.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val isDarkMode by sharedViewModel.isDarkMode.collectAsState()

    val cameraController = remember { CameraCaptureController() }
    var captureTarget by remember { mutableStateOf<Orientation?>(null) }

    val colors = rememberScreenColors(isDarkMode)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.golden_sample_title),
                        fontWeight = FontWeight.Bold,
                        color = colors.textColor
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Camera Area (Real CameraPreview)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp))
                    .background(Color.Black, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                CameraPreview(
                    controller = cameraController,
                    onFrameCaptured = { bitmap ->
                        captureTarget?.let { face ->
                            viewModel.captureFrame(bitmap, face)
                            captureTarget = null
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Control Panel
            Surface(
                color = colors.cardBackground,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, colors.cardBorder),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        stringResource(R.string.blueprint_config),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = colors.textColor
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = blueprintName,
                        onValueChange = { viewModel.updateName(it) },
                        label = { Text(stringResource(R.string.part_code_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tipo de geometría: Pieza irregular / No cuadrada (Aristas y Bordes)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0284C7).copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, Color(0xFF0284C7)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Geometría: Pieza irregular (No cuadrada)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textColor
                                )
                                Text(
                                    "Evaluación métrica de cada arista (E1, E2...), bordes y vértices. Espesor omitido.",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF0284C7),
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text(
                                    "ARISTAS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.has_two_faces_label),
                                color = colors.textColor,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                            Text(
                                "Evalúa por separado Anverso (avellanados y aristas) y Reverso (pasantes y bordes)",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = hasTwoFaces,
                            onCheckedChange = { viewModel.toggleTwoFaces(it) }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Botones de captura de caras
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                captureTarget = Orientation.ANVERSO
                                cameraController.triggerCapture()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "ANVERSO (${obverseFeatures.size} cotas)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (hasTwoFaces) {
                            Button(
                                onClick = {
                                    captureTarget = Orientation.REVERSO
                                    cameraController.triggerCapture()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFEA580C)
                                )
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "REVERSO (${reverseFeatures.size} cotas)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Ficha resumen dimensional de ambas caras
                    if (obverseFeatures.isNotEmpty() || reverseFeatures.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFF1F5F9)
                            ),
                            border = BorderStroke(1.dp, colors.cardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.PrecisionManufacturing,
                                        contentDescription = null,
                                        tint = colors.accentColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Metrología Integral Capturada:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = colors.textColor
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "• [ANVERSO]: $obverseSummary",
                                    fontSize = 11.sp,
                                    color = if (obverseFeatures.isNotEmpty()) Color(0xFF0284C7) else Color.Gray,
                                    fontWeight = FontWeight.Medium
                                )
                                if (hasTwoFaces) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "• [REVERSO]: $reverseSummary",
                                        fontSize = 11.sp,
                                        color = if (reverseFeatures.isNotEmpty()) Color(0xFFEA580C) else Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val canSave =
                        obverseFeatures.isNotEmpty() && (!hasTwoFaces || reverseFeatures.isNotEmpty())
                    Button(
                        onClick = {
                            viewModel.saveBlueprint { navController.popBackStack() }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = canSave && blueprintName.isNotBlank() && !isSaving,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF059669),
                            disabledContainerColor = colors.cardBorder
                        )
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.save_blueprint_btn),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}
