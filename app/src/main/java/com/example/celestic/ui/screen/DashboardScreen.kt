package com.example.celestic.ui.screen

import android.content.res.Configuration
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ModelTraining
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.celestic.R
import com.example.celestic.ui.component.ApprovedResultDialog
import com.example.celestic.ui.component.CameraPreview
import com.example.celestic.ui.theme.CelesticTheme
import com.example.celestic.viewmodel.DashboardViewModel
import com.example.celestic.viewmodel.DashboardViewModel.DashboardState
import com.example.celestic.viewmodel.SharedViewModel


/**
 * Main inspection dashboard screen.
 * 
 * Provides a live view of the automated inspection pipeline, including
 * real-time camera feed, batch status, and access to secondary tools like
 * calibration, blueprint training, and history.
 * 
 * @param navController Navigation controller for app flow.
 * @param viewModel Main view model managing the inspection state machine.
 * @param sharedViewModel Shared view model for user preferences and theme.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@UiComposable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel(),
    sharedViewModel: SharedViewModel = hiltViewModel()
) {
    val dashboardState = viewModel.state.collectAsState().value
    val isDarkMode = sharedViewModel.isDarkMode.collectAsState().value

    // Orientación sin BoxWithConstraints
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val cameraController = remember { com.example.celestic.ui.component.CameraCaptureController() }
    
    // Colores
    val accentColor = if (isDarkMode) Color(0xFF4FC3F7) else Color(0xFF3366CC)
    val frameBorder = if (isDarkMode) Color(0xFF1B263B) else Color(0xFFD1D9E6)
    val textPrimary = if (isDarkMode) Color.White else Color.Black
    val textSecondary = if (isDarkMode) Color.Gray else Color.DarkGray

    val mainBackground = Brush.verticalGradient(
        colors = if (isDarkMode) {
            listOf(Color(0xFF0A0E14), Color.Black)
        } else {
            listOf(Color(0xFFF2F2F2), Color(0xFFE0E0E0))
        }
    )

    CompositionLocalProvider(com.example.celestic.ui.component.LocalCameraCaptureController provides cameraController) {
        Scaffold(
            topBar = {
                dashboardState is DashboardState.Idle || dashboardState is DashboardState.NavigateToDetails
                dashboardState is DashboardState.Idle || dashboardState is DashboardState.NavigateToDetails
                dashboardState is DashboardState.Idle || dashboardState is DashboardState.NavigateToDetails
                DashboardTopBar(
                    state = dashboardState,
                    isLandscape = isLandscape,
                    isDarkMode = isDarkMode,
                    accentColor = accentColor,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    navController = navController,
                    viewModel = viewModel
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            DashboardMainContent(
                paddingValues = paddingValues,
                state = dashboardState,
                isLandscape = isLandscape,
                isDarkMode = isDarkMode,
                accentColor = accentColor,
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                frameBorder = frameBorder,
                mainBackground = mainBackground,
                viewModel = viewModel,
                navController = navController,
                cameraController = cameraController
            )
        }
        DashboardModals(
            state = dashboardState,
            viewModel = viewModel,
            navController = navController,
            context = androidx.compose.ui.platform.LocalContext.current
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardTopBar(
    state: DashboardState,
    isLandscape: Boolean,
    isDarkMode: Boolean,
    accentColor: Color,
    textPrimary: Color,
    textSecondary: Color,
    navController: NavController,
    viewModel: DashboardViewModel
) {
    var showAlbaranDialog by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf(
            false
        )
    }
    val context = androidx.compose.ui.platform.LocalContext.current
    var currentAlbaran by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf(
            context.getSharedPreferences("celestic_prefs", android.content.Context.MODE_PRIVATE)
                .getString("current_albaran", "GENERAL") ?: "GENERAL"
        )
    }
    var albaranText by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }

    if (showAlbaranDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAlbaranDialog = false },
            title = { Text(stringResource(R.string.assign_batch), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(stringResource(R.string.enter_batch_prompt), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.material3.OutlinedTextField(
                        value = albaranText,
                        onValueChange = { albaranText = it },
                        label = { Text(stringResource(R.string.batch_number)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val prefs = context.getSharedPreferences(
                        "celestic_prefs",
                        android.content.Context.MODE_PRIVATE
                    )
                    val finalAlbaran =
                        albaranText.ifBlank { context.getString(R.string.general_batch) }
                    prefs.edit { putString("current_albaran", finalAlbaran) }
                    currentAlbaran = finalAlbaran
                    showAlbaranDialog = false
                }) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAlbaranDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Surface(
        color = if (isDarkMode) Color.Black else Color.White,
        shadowElevation = 4.dp,
        modifier = Modifier.padding(
            horizontal = if (isLandscape) 16.dp else 8.dp,
            vertical = if (isLandscape) 4.dp else 2.dp
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            if (isDarkMode) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f)
        )
    ) {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PrecisionManufacturing,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(if (isLandscape) 22.dp else 18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.app_name).uppercase(),
                        fontSize = if (isLandscape) 16.sp else 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = if (isLandscape) 2.sp else 1.sp,
                        color = textPrimary
                    )
                }
            },
            actions = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val showActions = state is DashboardState.Idle || state is DashboardState.NavigateToDetails

                    if (showActions) {
                        NavIconBtn(
                            Icons.Default.Build,
                            stringResource(R.string.cal_abbr),
                            isLandscape,
                            isDarkMode
                        ) { navController.navigate("calibration") }

                        NavIconBtn(
                            Icons.Default.ModelTraining,
                            stringResource(R.string.plan_abbr),
                            isLandscape,
                            isDarkMode
                        ) { navController.navigate("golden_sample") }

                        NavIconBtn(
                            Icons.Default.History,
                            stringResource(R.string.hist_abbr),
                            isLandscape,
                            isDarkMode
                        ) { navController.navigate("detection_list") }

                        NavIconBtn(
                            Icons.Default.Assessment,
                            stringResource(R.string.rep_abbr),
                            isLandscape,
                            isDarkMode
                        ) { navController.navigate("reports") }

                        TextButton(
                            onClick = {
                                albaranText = currentAlbaran
                                showAlbaranDialog = true
                            },
                            modifier = Modifier.padding(end = 4.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text(
                                stringResource(R.string.batch_abbr, currentAlbaran),
                                color = textPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = if (isLandscape) 12.sp else 10.sp
                            )
                        }

                        IconButton(
                            onClick = { navController.navigate("settings") },
                            modifier = Modifier.size(if (isLandscape) 40.dp else 32.dp)
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                null,
                                tint = textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(if (isLandscape) 8.dp else 4.dp))
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            windowInsets = WindowInsets(0)
        )
    }
}

@Composable
private fun DashboardMainContent(
    paddingValues: PaddingValues,
    state: DashboardState,
    isLandscape: Boolean,
    isDarkMode: Boolean,
    accentColor: Color,
    textPrimary: Color,
    textSecondary: Color,
    frameBorder: Color,
    mainBackground: Brush,
    viewModel: DashboardViewModel,
    navController: NavController,
    cameraController: com.example.celestic.ui.component.CameraCaptureController
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(mainBackground)
            .padding(paddingValues)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isLandscape) 12.dp else 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(2.dp, frameBorder, RoundedCornerShape(20.dp))
                    .background(if (isDarkMode) Color.Black else Color.White)
            ) {
                CornerDecorations(accentColor)

                Crossfade(targetState = state, label = "dashboardContent") { currentState ->
                    when (currentState) {
                        DashboardState.Idle -> StandbyView(isLandscape, accentColor, textPrimary)
                        DashboardState.CameraReady -> DashboardCameraView(isLandscape, viewModel, cameraController)
                        DashboardState.Processing -> LoadingView(accentColor, textPrimary)
                        is DashboardState.Approved -> SuccessView(isLandscape)
                        is DashboardState.Rejected -> ErrorView(isLandscape, viewModel)
                        is DashboardState.Error -> ErrorView(isLandscape, viewModel)
                        is DashboardState.NavigateToDetails -> {
                            LoadingView(accentColor, textPrimary)
                        }
                        else -> {}
                    }
                }

                // New Centered Inspection Control Button
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = if (isLandscape) 24.dp else 40.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    val isIdle =
                        state is DashboardState.Idle || state is DashboardState.NavigateToDetails

                    Button(
                        onClick = {
                            if (isIdle) viewModel.startInspection()
                            else viewModel.resetState()
                        },
                        modifier = Modifier
                            .height(if (isLandscape) 56.dp else 64.dp)
                            .width(if (isLandscape) 140.dp else 180.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isIdle) Color(0xFF00695C) else Color(0xFFB71C1C)
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 12.dp),
                        shape = RoundedCornerShape(if (isLandscape) 28.dp else 32.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isIdle) Icons.Default.PlayArrow else Icons.Default.Stop,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(if (isLandscape) 20.dp else 24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (isIdle) stringResource(R.string.start).uppercase()
                                else stringResource(R.string.stop).uppercase(),
                                fontWeight = FontWeight.Black,
                                fontSize = if (isLandscape) 14.sp else 16.sp,
                                letterSpacing = 2.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            if (!isLandscape) {
                Text(
                    stringResource(R.string.system_version),
                    color = textSecondary.copy(alpha = 0.4f),
                    fontSize = 9.sp,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

/**
 * Reusable icon button for the top navigation bar.
 * 
 * Provides a consistent style for dashboard actions with support
 * for both landscape and portrait layouts.
 */
@Composable
fun NavIconBtn(
    icon: ImageVector,
    label: String,
    isLandscape: Boolean,
    isDarkMode: Boolean,
    onClick: () -> Unit
) {
    if (isLandscape) {
        TextButton(onClick = onClick, contentPadding = PaddingValues(horizontal = 8.dp)) {
            Icon(
                icon,
                null,
                modifier = Modifier.size(16.dp),
                tint = if (isDarkMode) Color(0xFF4FC3F7) else Color(0xFF3366CC)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color.LightGray else Color.DarkGray
            )
        }
    } else {
        IconButton(onClick = onClick, modifier = Modifier.size(32.dp)) {
            Icon(
                icon,
                null,
                modifier = Modifier.size(18.dp),
                tint = if (isDarkMode) Color(0xFF4FC3F7) else Color(0xFF3366CC)
            )
        }
    }
}

@Composable
fun StandbyView(isLandscape: Boolean, accentColor: Color, textPrimary: Color) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.PrecisionManufacturing,
                contentDescription = null,
                modifier = Modifier.size(if (isLandscape) 100.dp else 80.dp),
                tint = textPrimary.copy(alpha = 0.05f)
            )
            Text(
                stringResource(R.string.standby),
                color = accentColor.copy(alpha = 0.4f),
                letterSpacing = 8.sp,
                fontSize = 12.sp,
                fontWeight = FontWeight.Light
            )
        }
    }
}

@Composable

fun DashboardCameraView(
    isLandscape: Boolean,
    viewModel: DashboardViewModel,
    cameraController: com.example.celestic.ui.component.CameraCaptureController
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(2.dp)
    ) {
        CameraPreview(
            onFrameCaptured = { bitmap -> viewModel.onFrameCaptured(bitmap) },
            controller = cameraController
        )
        HUDOverlay(isLandscape)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isLandscape) 16.dp else 12.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Round3DInspectionButton(onClick = { cameraController.triggerCapture() })
        }
    }
}

@Composable
fun LoadingView(accentColor: Color, textPrimary: Color) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = accentColor,
                modifier = Modifier.size(40.dp),
                strokeWidth = 3.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                stringResource(R.string.analyzing),
                color = textPrimary,
                fontSize = 11.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun SuccessView(isLandscape: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF00332A)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.CheckCircle,
                null,
                tint = Color(0xFF00E676),
                modifier = Modifier.size(if (isLandscape) 64.dp else 48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                stringResource(R.string.success),
                color = Color(0xFF00E676),
                fontSize = if (isLandscape) 24.sp else 18.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun ErrorView(isLandscape: Boolean, viewModel: DashboardViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF330000))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Error,
            null,
            tint = Color.Red,
            modifier = Modifier.size(if (isLandscape) 64.dp else 48.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            stringResource(R.string.inspection_failed),
            color = Color.Red,
            fontSize = if (isLandscape) 18.sp else 14.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { viewModel.resetState() },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(stringResource(R.string.reset_system), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
@UiComposable
fun DashboardModals(
    state: DashboardState,
    viewModel: DashboardViewModel,
    navController: NavController,
    context: android.content.Context
) {
    if (state is DashboardState.Approved) {
        val detectionId = state.detectionId
        ApprovedResultDialog(
            onNewInspection = { viewModel.startNewInspection() },
            onViewReport = { navController.navigate("reports") },
            onGoToDetail = {
                navController.navigate("details/general?id=$detectionId")
                viewModel.resetState()
            }
        )
    }

    if (state is DashboardState.NavigateToDetails) {
        val detectionId = state.detectionId
        LaunchedEffect(detectionId) {
            navController.navigate("details/general?id=$detectionId")
            viewModel.resetState()
        }
    }

    if (state is DashboardState.Rejected) {
        val detectionId = state.detectionId
        LaunchedEffect(detectionId) {
            navController.navigate("details/general?id=$detectionId")
            viewModel.resetState()
        }
    }
}

@Composable
fun Round3DInspectionButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color(0xFF1B263B),
        shadowElevation = 8.dp,
        modifier = Modifier
            .size(64.dp)
            .border(
                width = 2.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(Color.White.copy(alpha = 0.3f), Color.Transparent)
                ),
                shape = CircleShape
            )
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(
                        Brush.radialGradient(colors = listOf(Color(0xFF415A77), Color(0xFF0D1B2A))),
                        CircleShape
                    )
            )
            Icon(Icons.Default.Camera, null, tint = Color.White, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
fun CornerDecorations(color: Color) {
    val thicknessDp = 3.dp
    val lengthDp = 30.dp
    Canvas(modifier = Modifier.fillMaxSize()) {
        val thickness = thicknessDp.toPx()
        val length = lengthDp.toPx()

        drawRect(color, Offset(0f, 0f), Size(length, thickness))
        drawRect(color, Offset(0f, 0f), Size(thickness, length))

        drawRect(color, Offset(size.width - length, 0f), Size(length, thickness))
        drawRect(color, Offset(size.width - thickness, 0f), Size(thickness, length))

        drawRect(color, Offset(0f, size.height - thickness), Size(length, thickness))
        drawRect(color, Offset(0f, size.height - length), Size(thickness, length))

        drawRect(
            color,
            Offset(size.width - length, size.height - thickness),
            Size(length, thickness)
        )
        drawRect(
            color,
            Offset(size.width - thickness, size.height - length),
            Size(thickness, length)
        )
    }
}

@Composable
fun HUDOverlay(isLandscape: Boolean) {
    val paddingDp = if (isLandscape) 80.dp else 40.dp
    val strokeWidthDp = 1.dp

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingDp)
    ) {
        val color = Color(0xFF4FC3F7).copy(alpha = 0.15f)
        val strokeWidth = strokeWidthDp.toPx()

        drawLine(
            color,
            Offset(size.width / 2 - 15, size.height / 2),
            Offset(size.width / 2 + 15, size.height / 2),
            strokeWidth
        )
        drawLine(
            color,
            Offset(size.width / 2, size.height / 2 - 15),
            Offset(size.width / 2, size.height / 2 + 20),
            strokeWidth
        )

        val bSize = 60f

        drawArc(
            color, 180f, 90f, false, Offset(0f, 0f), Size(bSize, bSize), style = Stroke(
                strokeWidth
        )
        )
        drawArc(
            color,
            270f,
            90f,
            false,
            Offset(size.width - bSize, 0f),
            Size(bSize, bSize),
            style = Stroke(strokeWidth)
        )
        drawArc(
            color,
            90f,
            90f,
            false,
            Offset(0f, size.height - bSize),
            Size(bSize, bSize),
            style = Stroke(strokeWidth)
        )
        drawArc(
            color,
            0f,
            90f,
            false,
            Offset(size.width - bSize, size.height - bSize),
            Size(bSize, bSize),
            style = Stroke(strokeWidth)
        )
    }
}

@Preview(showBackground = true, device = "spec:width=1280dp,height=800dp,orientation=landscape")
@Composable
fun DashboardScreenPreviewLandscape() {
    CelesticTheme {
        DashboardScreen(rememberNavController())
    }
}
