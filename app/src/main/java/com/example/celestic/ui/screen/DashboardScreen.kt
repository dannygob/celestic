package com.example.celestic.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.celestic.ui.theme.CelesticTheme

@Composable
fun DashboardScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Estado del Sistema",
            style = MaterialTheme.typography.titleLarge
        )

        Text(text = "Frames procesados: 2040")
        Text(text = "Alodines detectados: 7")
        Text(text = "Avellanados detectados: 4")
        Text(text = "Uso de CPU: 38%")
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDashboardScreen() {
    CelesticTheme {
        DashboardScreen()
    }
}