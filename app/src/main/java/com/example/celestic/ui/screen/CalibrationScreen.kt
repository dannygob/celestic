package com.example.celestic.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.celestic.ui.component.CameraPreview
import com.example.celestic.ui.component.triggerCameraCapture
import com.example.celestic.ui.theme.CelesticTheme
import com.example.celestic.viewmodel.CalibrationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalibrationScreen(
    navController: NavController,
    sharedViewModel: com.example.celestic.viewmodel.SharedViewModel = hiltViewModel(),
    calibrationViewModel: CalibrationViewModel = hiltViewModel()
) {
    LocalContext.current
    val isDarkMode by sharedViewModel.isDarkMode.collectAsState()
    val uiState by calibrationViewModel.uiState.collectAsState()

    val background = if (isDarkMode) Color(0xFF0A0E14) else Color(0xFFF2F2F2)
    val topBarBg = if (isDarkMode) Color.Black else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black
    val accentColor = if (isDarkMode) Color(0xFF4FC3F7) else Color(0xFF3366CC)
    val cardBg =
        if (isDarkMode) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
    ) {
        val isLandscape = maxWidth > maxHeight

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "CALIBRACIÓN",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                color = textColor
                            )
                            uiState.calibrationDate?.let {
                                Text("Última: $it", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Regresar",
                                tint = textColor
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { calibrationViewModel.reset() }) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Reiniciar",
                                tint = textColor
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = topBarBg,
                        titleContentColor = textColor
                    )
                )
            },
            containerColor = background
        ) { paddingValues ->
            if (isLandscape) {
                // DISEÑO LANDSCAPE (Cámara izquierda, Controles derecha)
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Área de Cámara (60% de ancho)
                    Box(
                        modifier = Modifier
                            .weight(1.5f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.Black)
                    ) {
                        CameraPreview(onFrameCaptured = { bitmap ->
                            calibrationViewModel.captureFrame(bitmap)
                        })

                        // Overlay de estado de captura (Mini)
                        uiState.lastCaptureSuccess?.let { success ->
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 8.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (success) Color.Green.copy(alpha = 0.8f) else Color.Red.copy(
                                            alpha = 0.8f
                                        )
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    if (success) "DETECTADO" else "NO VISIBLE",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Panel de Control (40% de ancho)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Estadísticas rápidamente legibles
                        Card(
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "CAPTURAS",
                                        color = Color.Gray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "${uiState.capturedFrames} / 15",
                                        color = accentColor,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                uiState.rmsError?.let { rms ->
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            "ERR RMS",
                                            color = Color.Gray,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "%.3f".format(rms),
                                            color = if (rms < 0.5) Color.Green else Color.Red,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = { triggerCameraCapture() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                        ) {
                            Icon(Icons.Default.Camera, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("CAPTURAR", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { calibrationViewModel.runCalibration() },
                            enabled = uiState.capturedFrames >= 5 && !uiState.isCalibrating,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1B263B),
                                disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                            )
                        ) {
                            if (uiState.isCalibrating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Science, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("CALIBRAR", fontWeight = FontWeight.Bold)
                            }
                        }

                        Text(
                            "Mueva el patrón a diferentes ángulos.",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else {
                // DISEÑO PORTRAIT (El original con mejoras de padding)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Panel de Control Superior
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "CAPTURAS",
                                color = Color.Gray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${uiState.capturedFrames} / 15",
                                color = accentColor,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        uiState.rmsError?.let { rms ->
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "ERROR RMS",
                                    color = Color.Gray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "%.4f".format(rms),
                                    color = if (rms < 0.5) Color.Green else Color.Red,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    // Área de Cámara
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.Black)
                    ) {
                        CameraPreview(onFrameCaptured = { bitmap ->
                            calibrationViewModel.captureFrame(bitmap)
                        })

                        uiState.lastCaptureSuccess?.let { success ->
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 16.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (success) Color.Green.copy(alpha = 0.8f) else Color.Red.copy(
                                            alpha = 0.8f
                                        )
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    if (success) "¡PATRÓN DETECTADO!" else "PATRÓN NO VISIBLE",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Acciones Inferiores
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = { triggerCameraCapture() },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                        ) {
                            Icon(Icons.Default.Camera, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("CAPTURAR", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { calibrationViewModel.runCalibration() },
                            enabled = uiState.capturedFrames >= 5 && !uiState.isCalibrating,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1B263B),
                                disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                            )
                        ) {
                            if (uiState.isCalibrating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Science, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("CALIBRAR", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Text(
                        "Mueva el patrón a diferentes ángulos.",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CalibrationScreenPreview() {
    CelesticTheme {
        CalibrationScreen(navController = rememberNavController())
    }
}