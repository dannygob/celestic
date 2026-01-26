package com.example.celestic.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun FeatureCard(
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    val cardPaddingDp = 8.dp
    val contentPaddingDp = 16.dp
    val spacerHeightDp = 8.dp
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(cardPaddingDp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation()
    ) {
        Column(
            Modifier.padding(contentPaddingDp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(spacerHeightDp))
            Text(text = description, textAlign = TextAlign.Center)
        }
    }
}
