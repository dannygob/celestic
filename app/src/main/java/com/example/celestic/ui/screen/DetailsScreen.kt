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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.graphics.Color
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
import com.example.celestic.models.DetectionItem
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
    detectionItem: DetectionItem? = null,
    detailsViewModel: DetailsViewModel? = null,
    sharedViewModel: SharedViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isDarkMode by sharedViewModel.isDarkMode.collectAsState()
    val colors = rememberScreenColors(isDarkMode)

    // Si no hay viewModel (Preview), usamos estados fijos
    val traceabilityResult = detailsViewModel?.traceabilityItem?.collectAsState()?.value
        ?: com.example.celestic.utils.Result.Loading
    val features = detailsViewModel?.features?.collectAsState()?.value ?: emptyList()
    val useInches = sharedViewModel.useInches.collectAsState().value

    val cardBg =
        if (isDarkMode) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)

    val reportSentMsg = stringResource(R.string.reportIssueSent)

    val title = when (detailType) {
        "hole" -> stringResource(R.string.detailsHole)
        "alodine" -> stringResource(R.string.detailsAlodine)
        "countersink" -> stringResource(R.string.detailsCountersink)
        else -> stringResource(R.string.detailedAnalysis)
    }

    LaunchedEffect(detectionItem) {
        detectionItem?.linkedQrCode?.let { code ->
            detailsViewModel?.loadTraceability(code)
        }
        detectionItem?.id?.let {
            detailsViewModel?.loadFeatures(it)
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
                    BlueprintView(features = features, useInches = useInches)
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

            // Botón de Reporte
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        Toast.makeText(context, reportSentMsg, Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDarkMode) Color(
                            0xFF330000
                        ) else Color(0xFFB00020)
                    )
                ) {
                    Icon(Icons.Default.ReportProblem, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.reportIssue), color = Color.White)
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