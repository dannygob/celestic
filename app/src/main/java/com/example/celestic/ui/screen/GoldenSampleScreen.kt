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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.celestic.R
import com.example.celestic.models.enums.Orientation
import com.example.celestic.ui.component.CameraCaptureController
import com.example.celestic.ui.component.CameraPreview
import com.example.celestic.viewmodel.GoldenSampleViewModel
import com.example.celestic.viewmodel.SharedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoldenSampleScreen(
    navController: NavController,
    viewModel: GoldenSampleViewModel = hiltViewModel(),
    sharedViewModel: SharedViewModel = hiltViewModel()
) {
    val blueprintName by viewModel.blueprintName.collectAsState()
    val hasTwoFaces by viewModel.hasTwoFaces.collectAsState()
    val obverseFeatures by viewModel.obverseFeatures.collectAsState()
    val reverseFeatures by viewModel.reverseFeatures.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val isDarkMode by sharedViewModel.isDarkMode.collectAsState()

    val cameraController = remember { CameraCaptureController() }
    var captureTarget by remember { mutableStateOf<Orientation?>(null) }

    val bgColor = if (isDarkMode) Color(0xFF121212) else Color(0xFFF5F5F5)
    val panelColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.golden_sample_title),
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
                    containerColor = panelColor,
                    titleContentColor = textColor
                )
            )
        },
        containerColor = bgColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Camera Area (Real CameraPreview)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Black, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                CameraPreview(
                    controller = cameraController,
                    onFrameCaptured = { bitmap ->
                        captureTarget?.let { face ->
                            viewModel.captureFrame(bitmap, face)
                            captureTarget = null
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Control Panel
            Surface(
                color = panelColor,
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        stringResource(R.string.blueprint_config),
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = blueprintName,
                        onValueChange = { viewModel.updateName(it) },
                        label = { Text(stringResource(R.string.part_code_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(R.string.has_two_faces_label),
                            color = textColor,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = hasTwoFaces,
                            onCheckedChange = { viewModel.toggleTwoFaces(it) })
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                captureTarget = Orientation.ANVERSO
                                cameraController.triggerCapture()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                stringResource(R.string.capture_obverse) + " (" + stringResource(
                                    R.string.pts_abbr,
                                    obverseFeatures.size
                                ) + ")", fontSize = 12.sp
                            )
                        }

                        if (hasTwoFaces) {
                            Button(
                                onClick = {
                                    captureTarget = Orientation.REVERSO
                                    cameraController.triggerCapture()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        0xFFF57C00
                                    )
                                )
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    stringResource(R.string.capture_reverse) + " (" + stringResource(
                                        R.string.pts_abbr,
                                        reverseFeatures.size
                                    ) + ")", fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val canSave =
                        obverseFeatures.isNotEmpty() && (!hasTwoFaces || reverseFeatures.isNotEmpty())
                    Button(
                        onClick = {
                            viewModel.saveBlueprint { navController.popBackStack() }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = canSave && blueprintName.isNotBlank() && !isSaving,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.save_blueprint_btn))
                    }
                }
            }
        }
    }
}
