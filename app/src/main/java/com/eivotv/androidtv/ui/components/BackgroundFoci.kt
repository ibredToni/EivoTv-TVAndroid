package com.eivotv.androidtv.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun BackgroundFoci(eivoColor: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Ancho intermedio: 12% de la pantalla
        val lineWeight = w * 0.12f

        // --- LÍNEA DEGRADADA IZQUIERDA ---
        drawRect(
            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                // Usamos 0.0f, 0.5f y 1.0f implícitamente para controlar la caída
                colors = listOf(
                    eivoColor.copy(alpha = 0.8f), // Más sólido en el extremo
                    eivoColor.copy(alpha = 0.2f),
                    Color.Transparent
                ),
                startX = 0f,
                endX = lineWeight
            ),
            size = androidx.compose.ui.geometry.Size(lineWeight, h)
        )

        // --- LÍNEA DEGRADADA DERECHA ---
        drawRect(
            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    eivoColor.copy(alpha = 0.2f),
                    eivoColor.copy(alpha = 0.8f)
                ),
                startX = w - lineWeight,
                endX = w
            ),
            topLeft = androidx.compose.ui.geometry.Offset(w - lineWeight, 0f),
            size = androidx.compose.ui.geometry.Size(lineWeight, h)
        )
    }
}