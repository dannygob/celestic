package com.example.celestic.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.celestic.ui.component.DetectionItemCard
import com.example.celestic.ui.component.ShimmerDetectionItemCard
import com.example.celestic.ui.theme.CelesticTheme
import com.example.celestic.utils.Result
import com.example.celestic.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun DetectionListScreen(
    navController: NavController,
    viewModel: MainViewModel = hiltViewModel(),
    sharedViewModel: com.example.celestic.viewmodel.SharedViewModel = hiltViewModel()
) {
    val detectionsResult by viewModel.detections.collectAsState()
    val isDarkMode by sharedViewModel.isDarkMode.collectAsState()

    val background = if (isDarkMode) Color(0xFF0A0E14) else Color(0xFFF2F2F2)
    val topBarBg = if (isDarkMode) Color.Black else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "HISTORIAL DE INSPECCIONES",
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
                            contentDescription = "Regresar",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
        ) {
            when (detectionsResult) {
                is Result.Loading -> {
                    items(5) {
                        ShimmerDetectionItemCard()
                    }
                }

                is Result.Success -> {
                    val detections = (detectionsResult as Result.Success).data
                    if (detections.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillParentMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No se encontraron registros.", color = Color.Gray)
                            }
                        }
                    } else {
                        items(detections) { item ->
                            DetectionItemCard(
                                item = item,
                                onClick = {
                                    // Navegar a detalles si fuera necesario
                                    navController.navigate("details/${item.id}")
                                }
                            )
                        }
                    }
                }

                is Result.Error -> {
                    item {
                        Box(
                            modifier = Modifier.fillParentMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Error al cargar el historial.",
                                color = Color.Red.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=1280dp,height=800dp,orientation=landscape")
@Composable
@UiComposable
fun DetectionListScreen() {
    CelesticTheme {
        DetectionListScreen(rememberNavController())
    }
}