package com.example.celestic.ui.theme


import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource

@Composable
fun CelesticTheme(content: @Composable () -> Unit) {
    val colorScheme = lightColorScheme(
        primary = colorResource(id = R.color.primary_blue),
        secondary = colorResource(id = R.color.secondary_teal),
        background = colorResource(id = R.color.surface_gray),
        surface = colorResource(id = R.color.surface_gray),
        error = colorResource(id = R.color.error_red),
        onPrimary = colorResource(id = R.color.on_primary),
        onBackground = colorResource(id = R.color.on_background)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CelesticTypography,
        content = content
    )
}
}