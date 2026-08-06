package com.eivotv.androidtv.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun Eivo_androidtvTheme(
    isInDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = darkColorScheme(
        primary = EivoTurquoise,
        background = EivoBackground,
        surface = EivoTurquoise.copy(alpha = 0.2f),
        onPrimary = androidx.compose.ui.graphics.Color.White,
        onBackground = androidx.compose.ui.graphics.Color.White
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}