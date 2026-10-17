package com.example.celestic.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.SettingsSuggest
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.celestic.R
import com.example.celestic.ui.theme.rememberScreenColors
import com.example.celestic.viewmodel.SharedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusScreen(
    navController: NavController,
    sharedViewModel: SharedViewModel = hiltViewModel()
) {
    val isDarkMode by sharedViewModel.isDarkMode.collectAsState()
    val colors = rememberScreenColors(isDarkMode)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.system_status),
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
                    titleContentColor = colors.textColor,
                    navigationIconContentColor = colors.textColor
                )
            )
        },
        containerColor = colors.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatusHeader(isDarkMode)

            StatusSection(
                title = stringResource(R.string.hardware_info),
                icon = Icons.Default.Memory,
                surfaceColor = colors.cardBackground,
                borderColor = colors.cardBorder,
                textColor = colors.textColor,
                textSecondary = colors.textSecondary
            ) {
                StatusItem(
                    label = stringResource(R.string.label_device),
                    value = sharedViewModel.deviceModel,
                    textColor = colors.textColor,
                    textSecondary = colors.textSecondary
                )
                StatusItem(
                    label = stringResource(R.string.label_hardware),
                    value = sharedViewModel.hardwareInfo,
                    textColor = colors.textColor,
                    textSecondary = colors.textSecondary
                )
            }

            StatusSection(
                title = stringResource(R.string.software_info),
                icon = Icons.Default.Dns,
                surfaceColor = colors.cardBackground,
                borderColor = colors.cardBorder,
                textColor = colors.textColor,
                textSecondary = colors.textSecondary
            ) {
                StatusItem(
                    label = stringResource(R.string.label_app_version),
                    value = "0.7.0-alpha",
                    textColor = colors.textColor,
                    textSecondary = colors.textSecondary
                )
                StatusItem(label = "OpenCV", value = "4.9.0", textColor = colors.textColor, textSecondary = colors.textSecondary)
                StatusItem(label = "TensorFlow Lite", value = "2.14.0", textColor = colors.textColor, textSecondary = colors.textSecondary)
            }

            StatusSection(
                title = stringResource(R.string.configuration),
                icon = Icons.Default.SettingsSuggest,
                surfaceColor = colors.cardBackground,
                borderColor = colors.cardBorder,
                textColor = colors.textColor,
                textSecondary = colors.textSecondary
            ) {
                val markerType by sharedViewModel.markerType.collectAsState()
                val units =
                    if (sharedViewModel.useInches.collectAsState().value) stringResource(R.string.label_inches) else stringResource(
                        R.string.label_millimeters
                    )
                StatusItem(
                    label = stringResource(R.string.label_marker_system),
                    value = markerType.name,
                    textColor = colors.textColor,
                    textSecondary = colors.textSecondary
                )
                StatusItem(
                    label = stringResource(R.string.label_measurement_units),
                    value = units,
                    textColor = colors.textColor,
                    textSecondary = colors.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.system_version),
                fontSize = 12.sp,
                color = colors.textTertiary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun StatusHeader(isDarkMode: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF0C4A6E) else Color(0xFFE0F2FE)
        ),
        border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF0284C7) else Color(0xFFBAE6FD))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = if (isDarkMode) Color(0xFF38BDF8) else Color(0xFF0284C7)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    stringResource(R.string.system_ready),
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = if (isDarkMode) Color(0xFFF0F4F8) else Color(0xFF0369A1)
                )
                Text(
                    stringResource(R.string.all_modules_initialized),
                    fontSize = 13.sp,
                    color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF0284C7)
                )
            }
        }
    }
}

@Composable
fun StatusSection(
    title: String,
    icon: ImageVector,
    surfaceColor: Color,
    borderColor: Color,
    textColor: Color,
    textSecondary: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = textSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, fontWeight = FontWeight.Bold, color = textSecondary, fontSize = 13.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            border = BorderStroke(1.dp, borderColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
fun StatusItem(label: String, value: String, textColor: Color, textSecondary: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = textSecondary, fontSize = 14.sp)
        Text(
            value,
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun StatusScreenPreview() {
    com.example.celestic.ui.theme.CelesticTheme {
        StatusScreen(navController = androidx.navigation.compose.rememberNavController())
    }
}
