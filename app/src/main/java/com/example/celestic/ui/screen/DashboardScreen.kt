package com.example.celestic.ui.screen

import android.content.res.Configuration
import android.widget.Toast
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
import com.example.celestic.models.enums.Orientation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HelpOutline
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
import androidx.compose.runtime.mutableStateOf
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

    val context = androidx.compose.ui.platform.LocalContext.current
    val cameraController = remember { com.example.celestic.ui.component.CameraCaptureController() }
    
    val onlineLearningMessage by viewModel.onlineLearningMessage.collectAsState()
    LaunchedEffect(onlineLearningMessage) {
        onlineLearningMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.clearOnlineLearningMessage()
        }
    }
    
    // Colores de alto contraste técnico
    val accentColor = if (isDarkMode) Color(0xFF38BDF8) else Color(0xFF0284C7)
    val frameBorder = if (isDarkMode) Color(0xFF26354D) else Color(0xFFCBD5E1)
    val textPrimary = if (isDarkMode) Color(0xFFF0F4F8) else Color(0xFF0F172A)
    val textSecondary = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF475569)

    val mainBackground = Brush.verticalGradient(
        colors = if (isDarkMode) {
            listOf(Color(0xFF070B11), Color(0xFF0F172A))
        } else {
            listOf(Color(0xFFF8FAFC), Color(0xFFE2E8F0))
        }
    )

    val editableEdges by viewModel.editableEdges.collectAsState()
    val blueprintName by viewModel.blueprintNameDisplay.collectAsState()
    var showEdgeAdjustDialog by remember { mutableStateOf(false) }

    if (showEdgeAdjustDialog) {
        com.example.celestic.ui.component.EdgeAdjustDialog(
            blueprintName = blueprintName,
            edges = editableEdges,
            onUpdateEdge = { edgeIndex, nom, tol -> viewModel.updateEdgeCota(edgeIndex, nom, tol) },
            onSave = { viewModel.saveAdjustedEdgesToBlueprint() },
            onReset = { viewModel.resetEdgesToBlueprint() },
            onAdjustToBlueprint = { viewModel.adjustAllEdgesToBlueprint() },
            onDismiss = { showEdgeAdjustDialog = false },
            isDarkMode = isDarkMode
        )
    }

    CompositionLocalProvider(com.example.celestic.ui.component.LocalCameraCaptureController provides cameraController) {
        Scaffold(
            topBar = {
                DashboardTopBar(
                    state = dashboardState,
                    isLandscape = isLandscape,
                    isDarkMode = isDarkMode,
                    accentColor = accentColor,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    navController = navController,
                    viewModel = viewModel,
                    onOpenEdgeAdjust = { showEdgeAdjustDialog = true }
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
                cameraController = cameraController,
                onOpenEdgeAdjust = { showEdgeAdjustDialog = true }
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
    viewModel: DashboardViewModel,
    onOpenEdgeAdjust: () -> Unit
) {
    var showAlbaranDialog by androidx.compose.runtime.remember {
        mutableStateOf(
            false
        )
    }
    val context = androidx.compose.ui.platform.LocalContext.current
    val isAiSlaveMode by viewModel.isAiSlaveMode.collectAsState()
    var showAiInfoDialog by androidx.compose.runtime.remember {
        mutableStateOf(false)
    }
    var currentAlbaran by androidx.compose.runtime.remember {
        mutableStateOf(
            context.getSharedPreferences("celestic_prefs", android.content.Context.MODE_PRIVATE)
                .getString("current_albaran", "GENERAL") ?: "GENERAL"
        )
    }
    var albaranText by androidx.compose.runtime.remember { mutableStateOf("") }
    var showOnlineLearningDialog by androidx.compose.runtime.remember {
        mutableStateOf(false)
    }
    var showInspectionWizardDialog by androidx.compose.runtime.remember {
        mutableStateOf(false)
    }
    val learnedSamplesCount by viewModel.learnedSamplesCount.collectAsState()
    val learnedSamples by viewModel.learnedSamples.collectAsState()

    if (showInspectionWizardDialog) {
        com.example.celestic.ui.component.InspectionWizardDialog(
            onDismiss = { showInspectionWizardDialog = false },
            onNavigateToCalibration = { navController.navigate("calibration") },
            isDarkMode = isDarkMode
        )
    }

    if (showOnlineLearningDialog) {
        com.example.celestic.ui.component.OnlineLearningDialog(
            learnedSamples = learnedSamples,
            onRollback = { viewModel.rollbackLastSample() },
            onResetAll = { viewModel.resetAllLearning() },
            onDeleteSample = { viewModel.deleteLearnedSample(it) },
            onDismiss = { showOnlineLearningDialog = false }
        )
    }

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

    if (showAiInfoDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAiInfoDialog = false },
            title = {
                Text(
                    text = if (isAiSlaveMode) stringResource(R.string.ai_mode_slave_title) else stringResource(
                        R.string.ai_mode_active_title
                    ),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        if (isAiSlaveMode) stringResource(R.string.ai_mode_slave_desc) else stringResource(
                            R.string.ai_mode_active_desc
                        ),
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAiInfoDialog = false }) {
                    Text(stringResource(R.string.ok))
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
                            Icons.Default.Architecture,
                            "ARISTAS",
                            isLandscape,
                            isDarkMode
                        ) { onOpenEdgeAdjust() }

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

                        NavIconBtn(
                            Icons.Default.HelpOutline,
                            stringResource(R.string.guide_abbr),
                            isLandscape,
                            isDarkMode
                        ) { showInspectionWizardDialog = true }

                        NavIconBtn(
                            Icons.Default.AutoAwesome,
                            "${stringResource(R.string.learning_abbr)} ($learnedSamplesCount)",
                            isLandscape,
                            isDarkMode
                        ) { showOnlineLearningDialog = true }

                        Surface(
                            onClick = { showAiInfoDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isAiSlaveMode) Color(0xFFE65100).copy(alpha = 0.15f) else Color(0xFF2E7D32).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, if (isAiSlaveMode) Color(0xFFFF9800) else Color(0xFF4CAF50)),
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isAiSlaveMode) Color(0xFFFF9800) else Color(
                                                0xFF4CAF50
                                            )
                                        )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isAiSlaveMode) stringResource(R.string.ai_tag_slave) else stringResource(
                                        R.string.ai_tag_tflite
                                    ),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isAiSlaveMode) Color(0xFFFFB74D) else Color(0xFF81C784)
                                )
                            }
                        }

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
    cameraController: com.example.celestic.ui.component.CameraCaptureController,
    onOpenEdgeAdjust: () -> Unit
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
            // Barra Técnica de Metrología: Cara de Inspección (Anverso/Reverso) y Cotas
            val currentFace by viewModel.currentInspectionFace.collectAsState()
            val editableEdges by viewModel.editableEdges.collectAsState()
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                border = BorderStroke(1.dp, frameBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Cara:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = textSecondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            onClick = { viewModel.setInspectionFace(Orientation.ANVERSO) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (currentFace == Orientation.ANVERSO) Color(0xFF0284C7) else Color.Transparent,
                            border = if (currentFace == Orientation.ANVERSO) null else BorderStroke(1.dp, frameBorder)
                        ) {
                            Text(
                                "ANVERSO (Cara A)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentFace == Orientation.ANVERSO) Color.White else textSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            onClick = { viewModel.setInspectionFace(Orientation.REVERSO) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (currentFace == Orientation.REVERSO) Color(0xFFEA580C) else Color.Transparent,
                            border = if (currentFace == Orientation.REVERSO) null else BorderStroke(1.dp, frameBorder)
                        ) {
                            Text(
                                "REVERSO (Cara B)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentFace == Orientation.REVERSO) Color.White else textSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Surface(
                        onClick = onOpenEdgeAdjust,
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0284C7).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(0xFF0284C7))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Architecture,
                                contentDescription = null,
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "ARISTAS PLANO: ${editableEdges.size} BORDES (AJUSTAR)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0284C7)
                            )
                        }
                    }
                }
            }

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
                            .width(if (isLandscape) 160.dp else 200.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isIdle) Color(0xFF059669) else Color(0xFFDC2626)
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 8.dp,
                            pressedElevation = 2.dp
                        ),
                        shape = RoundedCornerShape(if (isLandscape) 28.dp else 32.dp),
                        border = BorderStroke(
                            2.dp,
                            if (isIdle) Color(0xFF34D399) else Color(0xFFF87171)
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isIdle) Icons.Default.PlayArrow else Icons.Default.Stop,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(if (isLandscape) 22.dp else 26.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (isIdle) stringResource(R.string.start).uppercase()
                                else stringResource(R.string.stop).uppercase(),
                                fontWeight = FontWeight.Black,
                                fontSize = if (isLandscape) 15.sp else 17.sp,
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
    val buttonBg = if (isDarkMode) Color(0xFF1E293B) else Color.White
    val buttonBorder = if (isDarkMode) Color(0xFF334155) else Color(0xFFCBD5E1)
    val iconColor = if (isDarkMode) Color(0xFF38BDF8) else Color(0xFF0284C7)
    val textColor = if (isDarkMode) Color(0xFFE2E8F0) else Color(0xFF1E293B)

    if (isLandscape) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(10.dp),
            color = buttonBg,
            border = BorderStroke(1.dp, buttonBorder),
            modifier = Modifier.padding(horizontal = 2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Icon(
                    icon,
                    null,
                    modifier = Modifier.size(16.dp),
                    tint = iconColor
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        }
    } else {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(10.dp),
            color = buttonBg,
            border = BorderStroke(1.dp, buttonBorder),
            modifier = Modifier.padding(horizontal = 2.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    icon,
                    null,
                    modifier = Modifier.size(18.dp),
                    tint = iconColor
                )
            }
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
    val editableEdges by viewModel.editableEdges.collectAsState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(2.dp)
    ) {
        CameraPreview(
            onFrameCaptured = { bitmap -> viewModel.onFrameCaptured(bitmap) },
            controller = cameraController
        )
        HUDOverlay(isLandscape, editableEdges)

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
    var hasLearnedCurrent by remember(state) { mutableStateOf(false) }

    if (state is DashboardState.Approved) {
        val detectionId = state.detectionId
        ApprovedResultDialog(
            onNewInspection = { viewModel.startNewInspection() },
            onViewReport = { navController.navigate("reports") },
            onGoToDetail = {
                navController.navigate("details/general?id=$detectionId")
                viewModel.resetState()
            },
            onLearnPiece = {
                viewModel.learnCurrentPiece()
                hasLearnedCurrent = true
            },
            isLearned = hasLearnedCurrent
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
fun HUDOverlay(isLandscape: Boolean, edges: List<DashboardViewModel.EditableEdge> = emptyList()) {
    val paddingDp = if (isLandscape) 80.dp else 40.dp
    val strokeWidthDp = 1.dp

    Box(modifier = Modifier.fillMaxSize()) {
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

        if (edges.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.Black.copy(alpha = 0.70f),
                border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.8f)),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp, start = 12.dp, end = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Architecture,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "COTAS TÉCNICAS DEL PLANO (TOLERANCIA: ±0.8 mm)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        edges.joinToString(" • ") { "${it.id}: ${"%.1f".format(it.nominalMm)}mm" },
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=1280dp,height=800dp,orientation=landscape")
@Composable
fun DashboardScreenPreviewLandscape() {
    CelesticTheme {
        DashboardScreen(rememberNavController())
    }
}
