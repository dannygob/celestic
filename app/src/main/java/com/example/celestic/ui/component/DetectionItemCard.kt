package com.example.celestic.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.celestic.models.DetectionItem

@Composable
fun DetectionItemCard(item: DetectionItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("ID: ${item.id}")
            Spacer(modifier = Modifier.height(8.dp))
            Text("Type: ${item.type}")
            Spacer(modifier = Modifier.height(8.dp))
            Text("Status: ${item.status}")
            Spacer(modifier = Modifier.height(8.dp))
            item.measurementMm?.let {
                Text("Measurement: $it mm")
            }
        }
    }
}
