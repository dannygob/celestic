package com.example.celestic.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
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
import com.example.celestic.ui.theme.rememberScreenColors
import com.example.celestic.viewmodel.CalibrationState
import com.example.celestic.viewmodel.CalibrationViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalibrationScreen(
    navController: NavController,
    sharedViewModel: com.example.celestic.viewmodel.SharedViewModel = hiltViewModel(),
    calibrationViewModel: CalibrationViewModel = hiltViewModel()
) {
    val isDarkMode by sharedViewModel.isDarkMode.collectAsState()
    val colors = rememberScreenColors(isDarkMode)
    val uiState by calibrationViewModel.uiState.collectAsState()
    val locale = Locale.getDefault()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE



    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Scaffold(
            topBar = {
                CalibrationTopBar(
                    uiState = uiState,
                    textColor = colors.textColor,
                    topBarBg = colors.topBarBg,
                    onBack = { navController.popBackStack() },
                    onReset = { calibrationViewModel.reset() }
                )
            },
            containerColor = colors.background
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
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
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
    paddingValues: PaddingValues,
    uiState: CalibrationState,
    viewModel: CalibrationViewModel,
    locale: Locale,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    val accentColor = if (isDarkMode) Color(0xFF4FC3F7) else Color(0xFF3366CC)
    val cardBg =
        if (isDarkMode) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)

    Row(
        modifier = modifier
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
    paddingValues: PaddingValues,
    uiState: CalibrationState,
    viewModel: CalibrationViewModel,
    locale: Locale,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    val accentColor = if (isDarkMode) Color(0xFF4FC3F7) else Color(0xFF3366CC)
    val cardBg =
        if (isDarkMode) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)

    Column(
        modifier = modifier
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
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onCapture,
        modifier = modifier.height(56.dp),
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
        modifier = modifier.height(56.dp),
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
private fun CaptureStatusOverlay(
    success: Boolean?,
    alignment: Alignment,
    modifier: Modifier = Modifier
) {
    success?.let { isSuccess ->
        Box(
            modifier = modifier
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