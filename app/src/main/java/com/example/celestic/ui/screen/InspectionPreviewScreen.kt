package com.example.celestic.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.celestic.ui.theme.CelesticTheme
import com.example.celestic.ui.theme.rememberScreenColors
import com.example.celestic.viewmodel.SharedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectionPreviewScreen(
    navController: NavController,
    sharedViewModel: SharedViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isDarkMode by sharedViewModel.isDarkMode.collectAsState()
    val colors = rememberScreenColors(isDarkMode)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "VISTA PREVIA DE INSPECCIÓN",
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
                            contentDescription = "Regresar",
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.ViewInAr,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = colors.accentColor
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "CARGANDO MODELO 3D",
                color = colors.textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Preparando visualización aumentada del componente detectado...",
                color = Color.Gray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = {
                    Toast.makeText(
                        context,
                        "Módulo de Realidad Aumentada no disponible en esta versión.",
                        Toast.LENGTH_LONG
                    ).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDarkMode) Color(
                        0xFF1B263B
                    ) else Color(0xFF3366CC)
                )
            ) {
                Text("VER COMPONENTES", fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
@UiComposable
fun InspectionPreviewScreenPreview() {
    CelesticTheme {
        InspectionPreviewScreen(navController = rememberNavController())
    }
}