package com.example.celestic.ui.screen

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.celestic.ui.component.ApprovedResultDialog
import com.example.celestic.ui.component.CameraPreview
import com.example.celestic.ui.component.triggerCameraCapture
import com.example.celestic.ui.theme.CelesticTheme
import com.example.celestic.viewmodel.DashboardState
import com.example.celestic.viewmodel.DashboardViewModel
import com.example.celestic.viewmodel.SharedViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel? = null,
    sharedViewModel: SharedViewModel = hiltViewModel()
) {
    val state = viewModel?.state?.collectAsState()?.value ?: DashboardState.Idle
    val isDarkMode = sharedViewModel.isDarkMode.collectAsState().value

    val accentColor = if (isDarkMode) Color(0xFF4FC3F7) else Color(0xFF3366CC)
    val frameBorder = if (isDarkMode) Color(0xFF1B263B) else Color(0xFFD1D9E6)
    val textPrimary = if (isDarkMode) Color.White else Color.Black
    val textSecondary = if (isDarkMode) Color.Gray else Color.DarkGray

    val mainBackground = Brush.verticalGradient(
        colors = if (isDarkMode) {
            listOf(Color(0xFF0A0E14), Color(0xFF000000))
        } else {
            listOf(Color(0xFFF2F2F2), Color(0xFFE0E0E0))
        }
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isLandscape = maxWidth.value > maxHeight.value

        Scaffold(
            topBar = {
                // TopBar adaptativo y compacto
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
                                    "CELESTIC",
                                    fontSize = if (isLandscape) 16.sp else 13.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = if (isLandscape) 2.sp else 1.sp,
                                    color = textPrimary
                                )
                            }
                        },
                        actions = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                NavIconBtn(
                                    Icons.Default.Build,
                                    "CAL",
                                    isLandscape,
                                    isDarkMode
                                ) { navController.navigate("calibration") }
                                NavIconBtn(
                                    Icons.Default.History,
                                    "HIST",
                                    isLandscape,
                                    isDarkMode
                                ) { navController.navigate("detection_list") }
                                NavIconBtn(
                                    Icons.Default.Assessment,
                                    "REP",
                                    isLandscape,
                                    isDarkMode
                                ) { navController.navigate("reports") }

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

                                Button(
                                    onClick = {
                                        if (state == DashboardState.Idle) viewModel?.startInspection()
                                        else viewModel?.resetState()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (state == DashboardState.Idle) Color(
                                            0xFF00695C
                                        ) else Color(0xFFB71C1C)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = if (isLandscape) 12.dp else 8.dp),
                                    modifier = Modifier.height(if (isLandscape) 36.dp else 30.dp)
                                ) {
                                    Text(
                                        if (state == DashboardState.Idle) "START" else "STOP",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                        windowInsets = WindowInsets(0)
                    )
                }
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(mainBackground)
                    .padding(paddingValues)
            ) {
                // Layout principal
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
                                DashboardState.Idle -> StandbyView(
                                    isLandscape,
                                    accentColor,
                                    textPrimary
                                )

                                DashboardState.CameraReady -> CameraView(isLandscape, viewModel)
                                DashboardState.Processing -> LoadingView(accentColor, textPrimary)
                                is DashboardState.Approved -> SuccessView(isLandscape)
                                is DashboardState.Error -> ErrorView(isLandscape, viewModel)
                                else -> {}
                            }
                        }
                    }

                    if (!isLandscape) {
                        Text(
                            "Sistema de Visión de Alta Precisión Celestic v2.0",
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

        // Modales y Efectos
        DashboardModals(state, viewModel, navController)
    }
}

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
                "STANDBY",
                color = accentColor.copy(alpha = 0.4f),
                letterSpacing = 8.sp,
                fontSize = 12.sp,
                fontWeight = FontWeight.Light
            )
        }
    }
}

@Composable
fun CameraView(isLandscape: Boolean, viewModel: DashboardViewModel?) {
    Box(modifier = Modifier
        .fillMaxSize()
        .padding(2.dp)) {
        CameraPreview(onFrameCaptured = { bitmap -> viewModel?.onFrameCaptured(bitmap) })
        HUDOverlay(isLandscape)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isLandscape) 16.dp else 12.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Round3DInspectionButton(onClick = { triggerCameraCapture() })
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
                "ANALIZing...",
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
                "SUCCESS",
                color = Color(0xFF00E676),
                fontSize = if (isLandscape) 24.sp else 18.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun ErrorView(isLandscape: Boolean, viewModel: DashboardViewModel?) {
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
            "INSPECTION FAILED",
            color = Color.Red,
            fontSize = if (isLandscape) 18.sp else 14.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { viewModel?.resetState() },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("RESET SYSTEM", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DashboardModals(
    state: DashboardState,
    viewModel: DashboardViewModel?,
    navController: NavController
) {
    if (state is DashboardState.Approved) {
        val detectionId = state.detectionId
        ApprovedResultDialog(
            onNewInspection = { viewModel?.startNewInspection() },
            onViewReport = { navController.navigate("report/$detectionId") }
        )
    }

    if (state is DashboardState.NavigateToDetails) {
        val detectionId = state.detectionId
        LaunchedEffect(detectionId) {
            navController.navigate("details/$detectionId")
            viewModel?.resetState()
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
        // Dibujar las 4 esquinas industriales
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
    Canvas(modifier = Modifier
        .fillMaxSize()
        .padding(paddingDp)) {
        val color = Color(0xFF4FC3F7).copy(alpha = 0.15f)
        val strokeWidth = strokeWidthDp.toPx()

        // El 'size' aquí es de DrawScope y es un objeto Size (Float)
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
            color,
            180f,
            90f,
            false,
            Offset(0f, 0f),
            Size(bSize, bSize),
            style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth)
        )
        drawArc(
            color,
            270f,
            90f,
            false,
            Offset(size.width - bSize, 0f),
            Size(bSize, bSize),
            style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth)
        )
        drawArc(
            color,
            90f,
            90f,
            false,
            Offset(0f, size.height - bSize),
            Size(bSize, bSize),
            style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth)
        )
        drawArc(
            color,
            0f,
            90f,
            false,
            Offset(size.width - bSize, size.height - bSize),
            Size(bSize, bSize),
            style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth)
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
