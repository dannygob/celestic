package com.example.celestic.ui.screen

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.celestic.ui.component.DetectionItemCard
import com.example.celestic.viewmodel.MainViewModel

@Composable
fun DetectionListScreen(
    navController: NavController,
    viewModel: MainViewModel = hiltViewModel()
) {
    val detections by viewModel.detections.collectAsState()

    LazyColumn {
        items(detections) { item ->
            DetectionItemCard(item = item)
        }
    }
}
