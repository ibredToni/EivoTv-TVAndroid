package com.example.eivo_androidtv.ui.screens.linking

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.example.eivo_androidtv.R
import com.example.eivo_androidtv.ui.components.BackgroundFoci
import com.example.eivo_androidtv.ui.components.QrImage
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import com.example.eivo_androidtv.ui.components.FilterButton

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun LoginLinkingScreen(
    onNavigateToHome: () -> Unit,
    viewModel: LinkingViewModel
) {
    val state = viewModel.uiState
    val colorScheme = MaterialTheme.colorScheme
    val eivoColor = colorScheme.primary
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenWidthPx = configuration.screenWidthDp.toFloat() * androidx.compose.ui.platform.LocalDensity.current.density
    val dynamicRadius = screenWidthPx * 1.2f
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        viewModel.loadQrAndListen()
    }

    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) {
            onNavigateToHome()
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Image(
            painter = painterResource(id = R.drawable.aficion),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.6f
        )

        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.radialGradient(
                    0.0f to Color.Transparent,
                    0.1f to Color.Black.copy(alpha = 0.4f),
                    0.5f to Color.Black.copy(alpha = 0.95f),
                    1.0f to Color.Black,
                    radius = dynamicRadius
                )
            )
        )

        BackgroundFoci(MaterialTheme.colorScheme.primary)

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_eivo_logo),
                contentDescription = "Eivo Logo",
                modifier = Modifier.fillMaxWidth(0.12f).padding(bottom = 20.dp),
                contentScale = ContentScale.FillWidth
            )

            val textStyle = androidx.compose.ui.text.TextStyle(
                color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center
            )

            Text(text = "Para vincular tu cuenta en esta TV\nescanea el código QR desde tu App de eivo", style = textStyle)

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier.size(160.dp).background(Color.White, shape = RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (state.uuid != null) {
                    QrImage(text = state.uuid ?: "", modifier = Modifier.fillMaxSize())
                } else {
                    androidx.compose.material.CircularProgressIndicator(color = eivoColor)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text(text = "o introduce el código manualmente en tu App", style = textStyle)
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier.width(240.dp).border(1.dp, Color.White.copy(alpha = 0.6f), RectangleShape)
                    .background(Color.Black.copy(alpha = 0.4f)).padding(all = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.uuid ?: "Cargando...",
                    color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            FilterButton(
                text = "Generar nuevo código",
                isActive = false,
                onClick = { viewModel.loadQrAndListen() },
                modifier = Modifier.focusRequester(focusRequester)
            )
        }
    }
}