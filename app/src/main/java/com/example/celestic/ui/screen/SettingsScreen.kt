package com.example.celestic.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.celestic.R
import com.example.celestic.ui.theme.CelesticTheme
import com.example.celestic.viewmodel.MarkerType
import com.example.celestic.viewmodel.SharedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@UiComposable
fun SettingsScreen(
    navController: NavController,
    sharedViewModel: SharedViewModel = hiltViewModel()
) {
    val useInches by sharedViewModel.useInches.collectAsState()
    val markerType by sharedViewModel.markerType.collectAsState()
    val isDarkMode by sharedViewModel.isDarkMode.collectAsState()

    val background = if (isDarkMode) Color(0xFF0A0E14) else Color(0xFFF2F2F2)
    val topBarBg = if (isDarkMode) Color.Black else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black
    val sectionColor = if (isDarkMode) Color(0xFF415A77) else Color(0xFF3366CC)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.settingsTitle),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = textColor
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.returnDesc),
                            tint = textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = topBarBg,
                    titleContentColor = textColor
                )
            )
        },
        containerColor = background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                stringResource(R.string.unitsPreferences),
                color = sectionColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            SettingsItem(
                title = stringResource(R.string.imperialSystem),
                subtitle = stringResource(R.string.imperialSystemDesc),
                icon = Icons.Default.Straighten,
                checked = useInches,
                isDarkMode = isDarkMode,
                onCheckedChange = { sharedViewModel.setUseInches(it) }
            )

            SettingsItem(
                title = stringResource(R.string.aprilTagDetection),
                subtitle = stringResource(R.string.aprilTagDetectionDesc),
                icon = Icons.Default.Tag,
                checked = markerType == MarkerType.APRILTAG,
                isDarkMode = isDarkMode,
                onCheckedChange = {
                    sharedViewModel.setMarkerType(if (it) MarkerType.APRILTAG else MarkerType.ARUCO)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                stringResource(R.string.appearance),
                color = sectionColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            SettingsItem(
                title = stringResource(R.string.darkMode),
                subtitle = stringResource(R.string.darkModeDesc),
                icon = Icons.Default.DisplaySettings,
                checked = isDarkMode,
                isDarkMode = isDarkMode,
                onCheckedChange = { sharedViewModel.setDarkMode(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                stringResource(R.string.hardwareOptics),
                color = sectionColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkMode) Color.White.copy(
                        alpha = 0.05f
                    ) else Color.Black.copy(alpha = 0.05f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.DeveloperBoard,
                            contentDescription = null,
                            tint = sectionColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                sharedViewModel.deviceModel.uppercase(),
                                color = textColor,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                            Text(
                                sharedViewModel.hardwareInfo,
                                color = Color.Gray,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        stringResource(R.string.opticsAutoAdjust),
                        color = Color.Gray.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
@UiComposable
fun SettingsItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    isDarkMode: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val cardBg =
        if (isDarkMode) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)
    val textColor = if (isDarkMode) Color.White else Color.Black
    val iconBoxBg = if (isDarkMode) Color(0xFF1B263B) else Color(0xFFD1D9E6)
    val accentColor = if (isDarkMode) Color(0xFF415A77) else Color(0xFF3366CC)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconBoxBg, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(subtitle, color = Color.Gray, fontSize = 12.sp)
            }
            
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = accentColor,
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = Color.DarkGray
                )
            )
        }
    }
}

@Preview(showBackground = true, locale = "en")
@Composable
fun SettingsScreen() {
    CelesticTheme {
        SettingsScreen(rememberNavController())
    }
}