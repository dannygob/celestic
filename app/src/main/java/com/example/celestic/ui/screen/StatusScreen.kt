package com.example.celestic.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material3.*
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
    val surfaceColor = if (isDarkMode) Color(0xFF1B263B) else Color.White

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.systemStatus),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background,
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
                title = stringResource(R.string.hardwareInfo),
                icon = Icons.Default.Memory,
                surfaceColor = surfaceColor,
                textColor = colors.textColor
            ) {
                StatusItem(
                    label = "Device",
                    value = sharedViewModel.deviceModel,
                    textColor = colors.textColor
                )
                StatusItem(
                    label = "Hardware",
                    value = sharedViewModel.hardwareInfo,
                    textColor = colors.textColor
                )
            }

            StatusSection(
                title = stringResource(R.string.softwareInfo),
                icon = Icons.Default.Dns,
                surfaceColor = surfaceColor,
                textColor = colors.textColor
            ) {
                StatusItem(label = "App Version", value = "0.7.0-alpha", textColor = colors.textColor)
                StatusItem(label = "OpenCV", value = "4.9.0", textColor = colors.textColor)
                StatusItem(label = "TensorFlow Lite", value = "2.14.0", textColor = colors.textColor)
            }

            StatusSection(
                title = stringResource(R.string.configuration),
                icon = Icons.Default.SettingsSuggest,
                surfaceColor = surfaceColor,
                textColor = colors.textColor
            ) {
                val markerType by sharedViewModel.markerType.collectAsState()
                val units =
                    if (sharedViewModel.useInches.collectAsState().value) "Inches" else "Millimeters"
                StatusItem(label = "Marker System", value = markerType.name, textColor = colors.textColor)
                StatusItem(label = "Measurement Units", value = units, textColor = colors.textColor)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Celestic High Precision Vision System",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable

fun StatusHeader(isDarkMode: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF0D47A1) else Color(0xFFE3F2FD)
        )
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = if (isDarkMode) Color.White else Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    "SYSTEM READY",
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = if (isDarkMode) Color.White else Color(0xFF0D47A1)
                )
                Text(
                    "All modules initialized correctly",
                    fontSize = 14.sp,
                    color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color(0xFF1976D2)
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
    textColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable

fun StatusItem(label: String, value: String, textColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, fontSize = 14.sp)
        Text(value, color = textColor, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}
