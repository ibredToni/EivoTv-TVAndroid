package com.example.eivo_androidtv.ui.screens.linking

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.eivo_androidtv.R
import com.example.eivo_androidtv.ui.components.BackgroundFoci
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.eivo_androidtv.utils.generateRandomCode

@Composable
fun LoginLinkingScreen(onNavigateToHome: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    val eivoColor = colorScheme.primary
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenWidthPx = configuration.screenWidthDp.toFloat() * androidx.compose.ui.platform.LocalDensity.current.density
    val dynamicRadius = screenWidthPx * 1.2f

    var pairingCode by remember { mutableStateOf("XFKFJDO56") }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black)
    ) {
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

        BackgroundFoci(eivoColor)

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
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(text = "Para vincular tu cuenta en esta TV\nescanea el código QR desde tu App de eivo", style = textStyle)

            Spacer(modifier = Modifier.height(24.dp))

            Image(
                painter = painterResource(id = R.drawable.qr),
                contentDescription = "QR",
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.White, shape = RoundedCornerShape(4.dp))
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(text = "o introduce el código manualmente en tu App", style = textStyle)

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .width(200.dp)
                    .border(1.dp, Color.White.copy(alpha = 0.6f), RectangleShape)
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = pairingCode,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            com.example.eivo_androidtv.ui.components.FilterButton(
                text = "Generar código",
                isActive = false,
                onClick = { pairingCode = generateRandomCode(9) }
            )

            androidx.tv.material3.Button(
                onClick = onNavigateToHome,
                modifier = Modifier.padding(top = 10.dp)
            ) {
                Text("Ir a Home (Debug)")
            }
        }
    }
}