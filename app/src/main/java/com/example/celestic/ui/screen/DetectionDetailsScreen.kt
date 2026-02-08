package com.example.celestic.ui.screen

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.celestic.R
import com.example.celestic.viewmodel.DashboardViewModel
import java.io.File

@Composable
fun DetectionDetailsScreen(
    navController: NavController,
    detectionId: String?,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val detectionIdLong = detectionId?.toLongOrNull() ?: return

    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var status by remember { mutableStateOf("") }

    LaunchedEffect(detectionIdLong) {
        val item = viewModel.getDetectionById(detectionIdLong)
        item?.let {
            val file = File(context.filesDir, "detection_images/${it.frameId}.jpg")
            if (file.exists()) {
                imageBitmap = BitmapFactory.decodeFile(file.absolutePath)
            }
            status = it.status.name
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        imageBitmap?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),  // ← CORRECTO: función de extensión
                contentDescription = "Blueprint",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            // Overlay de estado
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .background(
                        if (status == "OK") Color.Green.copy(alpha = 0.8f)
                        else Color.Red.copy(alpha = 0.8f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.statusLabel, status),
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            // Botón volver
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(32.dp)
            ) {
                Text(stringResource(R.string.returnToDashboard))
            }
        } ?: CircularProgressIndicator()
    }
}