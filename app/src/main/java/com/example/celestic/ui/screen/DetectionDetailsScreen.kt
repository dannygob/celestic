package com.example.celestic.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.celestic.detector.model.BoundingBox
import com.example.celestic.detector.model.DetectionItem
import com.example.celestic.detector.model.DetectionType
import com.example.celestic.ui.theme.CelesticTheme
import com.example.celestica.R

@Composable
fun DetectionDetailsScreen(
    imagePlaceholder: Painter,
    detectionData: DetectionItem,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.surface_gray))
    ) {
        Image(
            painter = imagePlaceholder,
            contentDescription = "Graph Placeholder",
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.DarkGray),
            contentScale = ContentScale.Crop
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.default_padding))
        ) {
            item {
                DetailText(label = "Tipo", value = detectionData.type.name)
                DetailText(
                    label = "Confianza",
                    value = "${(detectionData.confidence * 100).format(2)}%"
                )
                DetailText(
                    label = "Medición",
                    value = "${detectionData.boundingBox.right - detectionData.boundingBox.left} mm"
                )
                DetailText(label = "Tiempo", value = detectionData.timestamp.toString())
                if (detectionData.notes.isNotBlank()) {
                    DetailText(label = "Notas", value = detectionData.notes)
                }
            }
        }
    }
}

@Composable
fun DetailText(label: String, value: String) {
    Text(
        text = "$label: $value",
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewDetectionDetails() {
    CelesticTheme {
        DetectionDetailsScreen(
            imagePlaceholder = painterResource(id = R.drawable.graph_placeholder),
            detectionData = DetectionItem(
                id = 1,
                type = DetectionType.HOLE,
                confidence = 0.88f,
                timestamp = System.currentTimeMillis(),
                boundingBox = BoundingBox(10f, 10f, 60f, 60f),
                notes = "Objeto detectado correctamente"
            )
        )
    }
}