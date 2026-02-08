package com.example.celestic.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.celestic.R
import com.example.celestic.ui.theme.CelesticTheme

@Composable

fun CameraScreen(navController: NavController) {
    val context = LocalContext.current
    val scanComponentText = stringResource(R.string.scanComponent)
    val loadingText = stringResource(R.string.loading)

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text(
            text = scanComponentText,
            style = MaterialTheme.typography.titleLarge
        )

        Button(onClick = {
            Toast.makeText(context, loadingText, Toast.LENGTH_SHORT).show()
            // Acción: iniciar escaneo
        }) {
            Text(scanComponentText)
        }
    }
}

@Preview(showBackground = true)
@Composable
@UiComposable
fun CameraScreenPreview() {
    CelesticTheme {
        CameraScreen(navController = rememberNavController())
    }
}