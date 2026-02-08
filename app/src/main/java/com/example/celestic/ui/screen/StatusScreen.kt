package com.example.celestic.ui.screen

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
import androidx.compose.ui.UiComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.celestic.R
import com.example.celestic.viewmodel.SharedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@UiComposable
fun StatusScreen(
    navController: NavController,
    sharedViewModel: SharedViewModel = hiltViewModel()
) {
    val isDarkMode by sharedViewModel.isDarkMode.collectAsState()
    val background = if (isDarkMode) Color(0xFF0A0E14) else Color(0xFFF2F2F2)
    val textColor = if (isDarkMode) Color.White else Color.Black
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
                    containerColor = background,
                    titleContentColor = textColor,
                    navigationIconContentColor = textColor
                )
            )
        },
        containerColor = background
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
                textColor = textColor
            ) {
                StatusItem(
                    label = "Device",
                    value = sharedViewModel.deviceModel,
                    textColor = textColor
                )
                StatusItem(
                    label = "Hardware",
                    value = sharedViewModel.hardwareInfo,
                    textColor = textColor
                )
            }

            StatusSection(
                title = stringResource(R.string.softwareInfo),
                icon = Icons.Default.Dns,
                surfaceColor = surfaceColor,
                textColor = textColor
            ) {
                StatusItem(label = "App Version", value = "0.7.0-alpha", textColor = textColor)
                StatusItem(label = "OpenCV", value = "4.9.0", textColor = textColor)
                StatusItem(label = "TensorFlow Lite", value = "2.14.0", textColor = textColor)
            }

            StatusSection(
                title = stringResource(R.string.configuration),
                icon = Icons.Default.SettingsSuggest,
                surfaceColor = surfaceColor,
                textColor = textColor
            ) {
                val markerType by sharedViewModel.markerType.collectAsState()
                val units =
                    if (sharedViewModel.useInches.collectAsState().value) "Inches" else "Millimeters"
                StatusItem(label = "Marker System", value = markerType.name, textColor = textColor)
                StatusItem(label = "Measurement Units", value = units, textColor = textColor)
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
@UiComposable
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
@UiComposable
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
@UiComposable
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
