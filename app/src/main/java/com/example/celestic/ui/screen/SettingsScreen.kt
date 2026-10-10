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
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.celestic.ui.theme.CelesticTheme
import com.example.celestic.ui.theme.rememberScreenColors
import com.example.celestic.viewmodel.MarkerType
import com.example.celestic.viewmodel.SharedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    sharedViewModel: SharedViewModel = hiltViewModel()
) {
    val useInches by sharedViewModel.useInches.collectAsState()
    val markerType by sharedViewModel.markerType.collectAsState()
    val isDarkMode by sharedViewModel.isDarkMode.collectAsState()
    val colors = rememberScreenColors(isDarkMode)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.settings_title),
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.topBarBg,
                    titleContentColor = colors.textColor
                )
            )
        },
        containerColor = colors.background
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
                stringResource(R.string.units_preferences),
                color = colors.accentColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            SettingsItem(
                title = stringResource(R.string.imperial_system),
                subtitle = stringResource(R.string.imperial_system_desc),
                icon = Icons.Default.Straighten,
                checked = useInches,
                isDarkMode = isDarkMode,
                onCheckedChange = { sharedViewModel.setUseInches(it) }
            )

            SettingsItem(
                title = stringResource(R.string.apriltag_detection),
                subtitle = stringResource(R.string.apriltag_detection_desc),
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
                color = colors.accentColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            SettingsItem(
                title = stringResource(R.string.dark_mode),
                subtitle = stringResource(R.string.dark_mode_desc),
                icon = Icons.Default.DisplaySettings,
                checked = isDarkMode,
                isDarkMode = isDarkMode,
                onCheckedChange = { sharedViewModel.setDarkMode(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                stringResource(R.string.hardware_optics),
                color = colors.accentColor,
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
                            tint = colors.accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                sharedViewModel.deviceModel.uppercase(),
                                color = colors.textColor,
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
                        stringResource(R.string.optics_auto_adjust),
                        color = Color.Gray.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── COMPANY DATA SECTION ─────────────────────────────────────────
            CompanyDataSection(isDarkMode = isDarkMode, accentColor = colors.accentColor)
        }
    }
}

@Composable
private fun CompanyDataSection(isDarkMode: Boolean, accentColor: Color) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("celestic_prefs", android.content.Context.MODE_PRIVATE)
    val cardBg = if (isDarkMode) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)
    val textColor = if (isDarkMode) Color.White else Color.Black
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = accentColor,
        unfocusedBorderColor = Color.Gray.copy(alpha = 0.4f),
        focusedLabelColor = accentColor,
        cursorColor = accentColor,
        focusedTextColor = textColor,
        unfocusedTextColor = textColor
    )

    var companyName    by remember { mutableStateOf(prefs.getString("company_name",    "") ?: "") }
    var companyAddress by remember { mutableStateOf(prefs.getString("company_address", "") ?: "") }
    var companyPhone   by remember { mutableStateOf(prefs.getString("company_phone",   "") ?: "") }
    var companyVat     by remember { mutableStateOf(prefs.getString("company_vat",     "") ?: "") }
    var qualityMgr     by remember { mutableStateOf(prefs.getString("company_quality_mgr", "") ?: "") }
    var saved          by remember { mutableStateOf(false) }

    Text(
        stringResource(R.string.company_settings_section).uppercase(),
        color = accentColor,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 4.dp)
    )
    Text(
        stringResource(R.string.company_settings_desc),
        color = Color.Gray,
        fontSize = 11.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Business, null, tint = accentColor, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.company_settings_section), color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            OutlinedTextField(
                value = companyName, onValueChange = { companyName = it; saved = false },
                label = { Text(stringResource(R.string.company_name_label)) },
                placeholder = { Text(stringResource(R.string.company_name_hint), color = Color.Gray) },
                singleLine = true, modifier = Modifier.fillMaxWidth(), colors = fieldColors
            )
            OutlinedTextField(
                value = companyAddress, onValueChange = { companyAddress = it; saved = false },
                label = { Text(stringResource(R.string.company_address_label)) },
                placeholder = { Text(stringResource(R.string.company_address_hint), color = Color.Gray) },
                singleLine = true, modifier = Modifier.fillMaxWidth(), colors = fieldColors
            )
            OutlinedTextField(
                value = companyPhone, onValueChange = { companyPhone = it; saved = false },
                label = { Text(stringResource(R.string.company_phone_label)) },
                placeholder = { Text(stringResource(R.string.company_phone_hint), color = Color.Gray) },
                singleLine = true, modifier = Modifier.fillMaxWidth(), colors = fieldColors
            )
            OutlinedTextField(
                value = companyVat, onValueChange = { companyVat = it; saved = false },
                label = { Text(stringResource(R.string.company_vat_label)) },
                placeholder = { Text(stringResource(R.string.company_vat_hint), color = Color.Gray) },
                singleLine = true, modifier = Modifier.fillMaxWidth(), colors = fieldColors
            )
            OutlinedTextField(
                value = qualityMgr, onValueChange = { qualityMgr = it; saved = false },
                label = { Text(stringResource(R.string.company_quality_mgr_label)) },
                placeholder = { Text(stringResource(R.string.company_quality_mgr_hint), color = Color.Gray) },
                singleLine = true, modifier = Modifier.fillMaxWidth(), colors = fieldColors
            )

            Button(
                onClick = {
                    prefs.edit()
                        .putString("company_name",        companyName)
                        .putString("company_address",     companyAddress)
                        .putString("company_phone",       companyPhone)
                        .putString("company_vat",         companyVat)
                        .putString("company_quality_mgr", qualityMgr)
                        .apply()
                    saved = true
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text(
                    if (saved) "✔  ${stringResource(R.string.company_data_saved)}"
                    else stringResource(R.string.save).uppercase(),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(32.dp))
}

    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    isDarkMode: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = rememberScreenColors(isDarkMode)
    val cardBg =
        if (isDarkMode) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)
    val iconBoxBg = if (isDarkMode) Color(0xFF1B263B) else Color(0xFFD1D9E6)

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
                Icon(icon, contentDescription = null, tint = colors.accentColor)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = colors.textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(subtitle, color = Color.Gray, fontSize = 12.sp)
            }
            
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = colors.accentColor,
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = Color.DarkGray
                )
            )
        }
    }
}

@Preview(showBackground = true, locale = "en")
@Composable
@UiComposable
fun SettingsScreenPreview() {
    CelesticTheme {
        SettingsScreen(rememberNavController())
    }
}