package com.example.celestic.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun DetectionDetailsScreen(
    navController: NavController,
    detectionId: String?,
    viewModel: com.example.celestic.viewmodel.DashboardViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val detectionIdLong = detectionId?.toLongOrNull() ?: return

    // Estado local para la imagen
    var imageBitmap by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf<android.graphics.Bitmap?>(
            null
        )
    }
    var status by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }

    androidx.compose.runtime.LaunchedEffect(detectionIdLong) {
        // En un caso real usaríamos un ViewModel específico, aquí reutilizamos Dashboard o Repo directo
        // para prototipado rápido.
        // Simulamos fetch manual o añadimos función al VM.
        // Vamos a asumir que el VM tiene una función 'getDetectionPath'.
        // Si no, lo hacemos aquí sucio para demostrar (Anti-pattern pero efectivo para demo inmediata)
        // Necesitamos 'frameId'
        // Mejor añadir 'getDetectionById' al VM.
        val item = viewModel.getDetectionById(detectionIdLong)
        item?.let {
            val file = java.io.File(context.filesDir, "detection_images/${it.frameId}.jpg")
            if (file.exists()) {
                imageBitmap = android.graphics.BitmapFactory.decodeFile(file.absolutePath)
            }
            status = it.status.name
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (imageBitmap != null) {
            androidx.compose.foundation.Image(
                bitmap = androidx.compose.ui.graphics.asImageBitmap(imageBitmap!!),
                contentDescription = "Blueprint",
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Fit
            )

            // Overlay de estado
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .background(
                        if (status == "OK") androidx.compose.ui.graphics.Color.Green.copy(alpha = 0.8f)
                        else androidx.compose.ui.graphics.Color.Red.copy(alpha = 0.8f),
                        androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    )
                    .padding(16.dp)
            ) {
                Text(
                    text = androidx.compose.ui.res.stringResource(
                        com.example.celestic.R.string.statusLabel,
                        status
                    ),
                    color = androidx.compose.ui.graphics.Color.White,
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge
                )
            }

            // Botón volver
            androidx.compose.material3.Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(32.dp)
            ) {
                Text(androidx.compose.ui.res.stringResource(com.example.celestic.R.string.returnToDashboard))
            }
        } else {
            androidx.compose.material3.CircularProgressIndicator()
        }
    }
}