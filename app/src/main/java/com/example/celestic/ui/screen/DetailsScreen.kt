package com.example.celestic.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.celestic.models.DetectionItem
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.celestic.models.DetectionItem
import com.example.celestic.viewmodel.DetailsViewModel

@Composable
fun DetailsScreen(
    navController: NavController,
    detailType: String,
    detectionItem: DetectionItem? = null,
    viewModel: DetailsViewModel = viewModel()
) {
    val context = LocalContext.current
    val title = when (detailType) {
        "hole" -> stringResource(R.string.details_title_hole)
        "alodine" -> stringResource(R.string.details_title_alodine)
        "countersink" -> stringResource(R.string.details_title_countersink)
        else -> "Detalle"
    }

    LaunchedEffect(detectionItem) {
        detectionItem?.linkedQrCode?.let { codigo ->
            viewModel.loadTrazabilidad(codigo)
        }
    }

    val trazabilidad by viewModel.trazabilidadItem.collectAsState()

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text(text = title, style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        trazabilidad?.let {
            Divider()
            Text("🔍 Trazabilidad:", style = MaterialTheme.typography.titleMedium)
            Text("• Código: ${it.codigo}")
            Text("• Pieza: ${it.pieza}")
            Text("• Operario: ${it.operario}")
            Text("• Fecha: ${it.fecha}")
            Text("• Resultado: ${it.resultado}")
        } ?: Text("❌ No hay información de trazabilidad.")

        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = {
            Toast.makeText(context, stringResource(R.string.report_issue), Toast.LENGTH_SHORT)
                .show()
        }) {
            Text(stringResource(R.string.report_issue))
        }
    }
}