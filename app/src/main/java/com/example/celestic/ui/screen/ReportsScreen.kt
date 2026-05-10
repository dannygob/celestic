package com.example.celestic.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.celestic.R
import com.example.celestic.ui.theme.rememberScreenColors
import com.example.celestic.viewmodel.ReportsViewModel
import com.example.celestic.viewmodel.SharedViewModel

/**
 * Screen for managing and generating inspection reports.
 * 
 * Lists detected batches (albaranes) from the history and allows
 * users to export consolidated technical reports in various formats.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    navController: NavController,
    viewModel: ReportsViewModel = hiltViewModel(),
    sharedViewModel: SharedViewModel = hiltViewModel()
) {
    val isDarkMode by sharedViewModel.isDarkMode.collectAsState()
    val colors = rememberScreenColors(isDarkMode)
    val batches by viewModel.batches.collectAsState()
    val allDetections by viewModel.allDetections.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.reports_title).uppercase(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = colors.textColor
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.return_desc),
                            tint = colors.textColor
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("detection_list") }) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = "History",
                            tint = colors.accentColor
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
            // General Statistics Header
            item {
                ReportStatsCard(
                    totalInspections = batches.size,
                    totalDetections = allDetections.size,
                    accentColor = colors.accentColor,
                    isDarkMode = isDarkMode
                )
            }

            item {
                Text(
                    stringResource(R.string.select_batch_report).uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textColor.copy(alpha = 0.6f),
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Batch List (Connection with History)
            if (batches.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.no_history_reports), color = Color.Gray)
                    }
                }
            } else {
                items(batches) { batchCode ->
                    BatchReportItem(
                        batchCode = batchCode,
                        count = viewModel.getBatchCount(batchCode),
                        accentColor = colors.accentColor,
                        isDarkMode = isDarkMode,
                        onGenerate = { viewModel.generateBatchReport(batchCode) }
                    )
                }
            }
        }
    }
}

@Composable
fun ReportStatsCard(
    totalInspections: Int,
    totalDetections: Int,
    accentColor: Color,
    isDarkMode: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Assessment, contentDescription = null, tint = accentColor)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    stringResource(R.string.report_overview),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    stringResource(
                        R.string.report_summary_stats,
                        totalInspections,
                        totalDetections
                    ),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun BatchReportItem(
    batchCode: String,
    count: Int,
    accentColor: Color,
    isDarkMode: Boolean,
    onGenerate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onGenerate,
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF161616) else Color(0xFFF9F9F9)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Description,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(batchCode, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    stringResource(R.string.detections_count, count),
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
            IconButton(onClick = onGenerate) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = accentColor)
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun ReportsScreenPreview() {
    com.example.celestic.ui.theme.CelesticTheme {
        ReportsScreen(navController = androidx.navigation.compose.rememberNavController())
    }
}
