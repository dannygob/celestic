package com.example.celestic.ui.screen

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.celestic.models.DetectionItem

@Composable
fun DetectionListScreen(navController: NavController, items: List<DetectionItem>) {
    LazyColumn {
        items(items) { item ->
            Text(text = item.description)
        }
    }
}
