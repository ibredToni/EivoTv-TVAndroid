package com.eivotv.androidtv.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.eivotv.androidtv.R
import com.eivotv.androidtv.ui.components.BackgroundFoci
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    eivoColor: Color,
    onSplashFinished: () -> Unit
) {
    val logoAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        logoAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )

        delay(2000)

        logoAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 1000)
        )

        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        BackgroundFoci(eivoColor = eivoColor)

        Image(
            painter = painterResource(id = R.drawable.ic_eivo_logo),
            contentDescription = "Eivo Logo",
            modifier = Modifier
                .size(200.dp)
                .alpha(logoAlpha.value)
        )
    }
}