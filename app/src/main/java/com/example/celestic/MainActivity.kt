package com.example.celestic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.celestic.navigation.AppNavigation
import com.example.celestic.ui.theme.CelesticTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CelesticTheme {
                AppNavigation()
            }
        }
    }
}