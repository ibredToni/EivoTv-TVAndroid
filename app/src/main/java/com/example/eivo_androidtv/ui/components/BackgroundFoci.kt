package com.example.eivo_androidtv.ui.components

import android.graphics.BlurMaskFilter
import android.graphics.LinearGradient
import android.graphics.Shader
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb

@Composable
fun BackgroundFoci(eivoColor: Color) {
    Canvas(modifier = Modifier
        .fillMaxSize()
        .graphicsLayer(renderEffect = null)
    ) {
        val w = size.width
        val h = size.height
        val dynamicBlur = w * 0.0625f

        drawIntoCanvas { canvas ->
            val paint = Paint().asFrameworkPaint().apply {
                isAntiAlias = true
                if (dynamicBlur > 0f) {
                    maskFilter = BlurMaskFilter(dynamicBlur, BlurMaskFilter.Blur.NORMAL)
                }
            }

            val pathIzquierdo = android.graphics.Path().apply {
                moveTo(0f, 0f)
                lineTo(w * 0.055f, 0f)
                lineTo(w * 0.01f, h * 0.995f)
                lineTo(0f, h * 0.995f)
                close()
            }

            paint.shader = LinearGradient(
                0f, 0f, 0f, h * 0.98f,
                eivoColor.copy(alpha = 0.9f).toArgb(),
                android.graphics.Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            )
            canvas.nativeCanvas.drawPath(pathIzquierdo, paint)

            val pathDerecho = android.graphics.Path().apply {
                moveTo(w, h * 0.02f)
                lineTo(w, h)
                lineTo(w * 0.975f, h)
                lineTo(w * 0.99f, h * 0.02f)
                close()
            }

            paint.shader = LinearGradient(
                w, h, w, h * 0.05f,
                eivoColor.copy(alpha = 0.85f).toArgb(),
                android.graphics.Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            )
            canvas.nativeCanvas.drawPath(pathDerecho, paint)
        }
    }
}