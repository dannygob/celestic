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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.celestic.R
import com.example.celestic.ui.component.CameraPreview
import com.example.celestic.ui.component.triggerCameraCapture
import com.example.celestic.ui.theme.CelesticTheme
import com.example.celestic.viewmodel.CalibrationState
import com.example.celestic.viewmodel.CalibrationViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun CalibrationScreen(
    navController: NavController,
    sharedViewModel: com.example.celestic.viewmodel.SharedViewModel = hiltViewModel(),
    calibrationViewModel: CalibrationViewModel = hiltViewModel()
) {
    val isDarkMode by sharedViewModel.isDarkMode.collectAsState()
    val uiState by calibrationViewModel.uiState.collectAsState()
    val locale = Locale.getDefault()

    val background = if (isDarkMode) Color(0xFF0A0E14) else Color(0xFFF2F2F2)
    val textColor = if (isDarkMode) Color.White else Color.Black
    val topBarBg = if (isDarkMode) Color.Black else Color.White

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
    ) {
        val isLandscape = maxWidth > maxHeight

        Scaffold(
            topBar = {
                CalibrationTopBar(
                    uiState = uiState,
                    textColor = textColor,
                    topBarBg = topBarBg,
                    onBack = { navController.popBackStack() },
                    onReset = { calibrationViewModel.reset() }
                )
            },
            containerColor = background
        ) { paddingValues ->
            if (isLandscape) {
                LandscapeCalibrationContent(
                    paddingValues = paddingValues,
                    uiState = uiState,
                    viewModel = calibrationViewModel,
                    locale = locale,
                    isDarkMode = isDarkMode
                )
            } else {
                PortraitCalibrationContent(
                    paddingValues = paddingValues,
                    uiState = uiState,
                    viewModel = calibrationViewModel,
                    locale = locale,
                    isDarkMode = isDarkMode
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable

private fun CalibrationTopBar(
    uiState: CalibrationState,
    textColor: Color,
    topBarBg: Color,
    onBack: () -> Unit,
    onReset: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    stringResource(R.string.calibrationTitle),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = textColor
                )
                uiState.calibrationDate?.let { date ->
                    Text(
                        stringResource(R.string.lastCalibration, date),
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.returnDesc),
                    tint = textColor
                )
            }
        },
        actions = {
            IconButton(onClick = onReset) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.reset),
                    tint = textColor
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = topBarBg,
            titleContentColor = textColor
        )
    )
}

@Composable

private fun LandscapeCalibrationContent(
    paddingValues: androidx.compose.foundation.layout.PaddingValues,
    uiState: CalibrationState,
    viewModel: CalibrationViewModel,
    locale: Locale,
    isDarkMode: Boolean
) {
    val accentColor = if (isDarkMode) Color(0xFF4FC3F7) else Color(0xFF3366CC)
    val cardBg =
        if (isDarkMode) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1.5f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.Black)
        ) {
            CameraPreview(onFrameCaptured = { viewModel.captureFrame(it) })
            CaptureStatusOverlay(uiState.lastCaptureSuccess, Alignment.TopCenter)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CalibrationStatsCard(uiState, accentColor, cardBg, locale)

            CalibrationActions(
                uiState = uiState,
                accentColor = accentColor,
                onCapture = { triggerCameraCapture() },
                onCalibrate = { viewModel.runCalibration() }
            )

            Text(
                stringResource(R.string.calibrationInstructions),
                color = Color.Gray,
                fontSize = 11.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable

private fun PortraitCalibrationContent(
    paddingValues: androidx.compose.foundation.layout.PaddingValues,
    uiState: CalibrationState,
    viewModel: CalibrationViewModel,
    locale: Locale,
    isDarkMode: Boolean
) {
    val accentColor = if (isDarkMode) Color(0xFF4FC3F7) else Color(0xFF3366CC)
    val cardBg =
        if (isDarkMode) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CalibrationStatsCard(
            uiState = uiState,
            accentColor = accentColor,
            cardBg = cardBg,
            locale = locale,
            modifier = Modifier.padding(16.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.Black)
        ) {
            CameraPreview(onFrameCaptured = { viewModel.captureFrame(it) })
            CaptureStatusOverlay(uiState.lastCaptureSuccess, Alignment.TopCenter)
        }

        CalibrationActions(
            uiState = uiState,
            accentColor = accentColor,
            onCapture = { triggerCameraCapture() },
            onCalibrate = { viewModel.runCalibration() },
            modifier = Modifier.padding(24.dp),
            isLandscape = false
        )

        Text(
            stringResource(R.string.calibrationInstructions),
            color = Color.Gray,
            fontSize = 11.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}

@Composable

private fun CalibrationStatsCard(
    uiState: CalibrationState,
    accentColor: Color,
    cardBg: Color,
    locale: Locale,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    stringResource(R.string.captures),
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
                        stringResource(R.string.rmsError),
                        color = Color.Gray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        String.format(locale, "%.4f", rms),
                        color = if (rms < 0.5) Color.Green else Color.Red,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable

private fun CalibrationActions(
    uiState: CalibrationState,
    accentColor: Color,
    onCapture: () -> Unit,
    onCalibrate: () -> Unit,
    modifier: Modifier = Modifier,
    isLandscape: Boolean = true
) {
    if (isLandscape) {
        Column(modifier = modifier) {
            CalibrationButtons(
                uiState,
                accentColor,
                onCapture,
                onCalibrate,
                Modifier.fillMaxWidth()
            )
        }
    } else {
        Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            CalibrationButtons(uiState, accentColor, onCapture, onCalibrate, Modifier.weight(1f))
        }
    }
}

@Composable

private fun CalibrationButtons(
    uiState: CalibrationState,
    accentColor: Color,
    onCapture: () -> Unit,
    onCalibrate: () -> Unit,
    buttonModifier: Modifier
) {
    Button(
        onClick = onCapture,
        modifier = buttonModifier.height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = accentColor)
    ) {
        Icon(Icons.Default.Camera, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(stringResource(R.string.Capture), fontWeight = FontWeight.Bold)
    }

    // El Spacer depende de si es Columna o Fila, pero podemos usar un Box o manejarlo fuera.
    // Para simplificar, lo manejamos en el contenedor (CalibrationActions).

    Button(
        onClick = onCalibrate,
        enabled = uiState.capturedFrames >= 5 && !uiState.isCalibrating,
        modifier = buttonModifier.height(56.dp),
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
            Text(
                stringResource(R.string.calibrate),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CaptureStatusOverlay(success: Boolean?, alignment: Alignment) {
    success?.let { isSuccess ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = alignment
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSuccess) Color.Green.copy(alpha = 0.8f)
                        else Color.Red.copy(alpha = 0.8f)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    if (isSuccess) stringResource(R.string.patternDetected)
                    else stringResource(R.string.patternNotVisible),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
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