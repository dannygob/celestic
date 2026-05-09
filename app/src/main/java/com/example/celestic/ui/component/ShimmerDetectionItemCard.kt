package com.example.celestic.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Placeholder card shown while detection items are loading.
 * Displays simple shimmer-like gray bars to simulate content.
 */
@Composable
fun ShimmerDetectionItemCard() {
    val cardPaddingDp = 8.dp
    val contentPaddingDp = 16.dp
    val shimmerHeightDp = 20.dp
    val spacerHeightDp = 8.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(cardPaddingDp)
    ) {
        Column(modifier = Modifier.padding(contentPaddingDp)) {

            // First shimmer bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(shimmerHeightDp)
                    .background(Color.LightGray.copy(alpha = 0.5f))
            )

            Spacer(modifier = Modifier.height(spacerHeightDp))

            // Second shimmer bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(shimmerHeightDp)
                    .background(Color.LightGray.copy(alpha = 0.5f))
            )

            Spacer(modifier = Modifier.height(spacerHeightDp))

            // Third shimmer bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(shimmerHeightDp)
                    .background(Color.LightGray.copy(alpha = 0.5f))
            )

            Spacer(modifier = Modifier.height(spacerHeightDp))

            // Shorter shimmer bar
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(shimmerHeightDp)
                    .background(Color.LightGray.copy(alpha = 0.5f))
            )
        }
    }
}
