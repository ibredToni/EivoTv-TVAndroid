package com.eivotv.androidtv.ui.screens.player

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.eivotv.androidtv.R
import com.eivotv.androidtv.data.remote.dto.AdDto
import com.eivotv.androidtv.data.remote.dto.EventoDetalleDto
import com.eivotv.androidtv.ui.components.BackgroundFoci
import com.eivotv.androidtv.utils.formatToLongDate
import kotlinx.coroutines.delay

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PauseScreen(
    onResume: () -> Unit,
    detalle: EventoDetalleDto,
    backgroundImage: String?,
    ads: List<AdDto>
) {
    BackHandler {
        onResume()
    }

    val pauseAd = ads.find { it.position == "pause" }
    val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val density = androidx.compose.ui.platform.LocalDensity.current

    val screenWidthPx = with(density) {
        configuration.screenWidthDp.dp.toPx()
    }

    val gradientEndEdge = screenWidthPx * .6f

    val formattedName = remember(detalle.nombre) {
        detalle.nombre?.replace(Regex("\\s*-\\s*"), "\n")?.uppercase() ?: ""
    }
    val titleStyle = MaterialTheme.typography.displayMedium
    val dynamicFontSize = when {
        formattedName.length > 30 -> titleStyle.fontSize * 0.7f
        formattedName.length > 20 -> titleStyle.fontSize * 0.85f
        else -> titleStyle.fontSize
    }

    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 1. Imagen de Publicidad (Background)
        AsyncImage(
            model = pauseAd?.url,
            contentDescription = "Publicidad",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            error = null
        )

        // 2. Gradiente de fondo para legibilidad
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF070A0D),
                            Color.Black,
                            Color.Transparent
                        ),
                        startX = 0f,
                        endX = gradientEndEdge
                    )
                )
        )

        BackgroundFoci(MaterialTheme.colorScheme.primary)

        // 3. Contenedor Lateral Izquierdo (Info del evento)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.30f)
                .padding(start = 58.dp, top = 58.dp, bottom = 58.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.dp, Color.White.copy(alpha = 0.5f), RectangleShape)
            ) {
                // Imagen de fondo del recuadro
                if (!backgroundImage.isNullOrBlank()) {
                    AsyncImage(
                        model = backgroundImage,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.Center
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.portero),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    // Logos de los equipos
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = detalle.img_loc,
                            contentDescription = null,
                            modifier = Modifier.size(100.dp),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(Modifier.width(8.dp))
                        AsyncImage(
                            model = detalle.img_vis,
                            contentDescription = null,
                            modifier = Modifier.size(100.dp),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    // Nombre del Evento
                    Text(
                        text = formattedName.uppercase(),
                        color = Color.White,
                        style = titleStyle.copy(
                            fontSize = dynamicFontSize,
                            lineHeight = dynamicFontSize * 1.1f
                        ),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(4.dp))

                    // Descripción
                    Text(
                        text = detalle.descripcion ?: "",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )

                    // Horario
                    Text(
                        text = formatToLongDate(detalle.inicio) ?: "",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Botón Continuar
                    Button(
                        onClick = onResume,
                        shape = ButtonDefaults.shape(RectangleShape),
                        colors = ButtonDefaults.colors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .height(20.dp)
                            .wrapContentWidth()
                            .focusRequester(focusRequester),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_play),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(2.dp))
                            Text(
                                text = stringResource(R.string.player_keep_watching),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }

        // 4. Etiqueta de Publicidad
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(30.dp)
                .width(34.dp)
                .height(30.dp)
                .background(
                    color = Color(0xFF636363).copy(alpha = 0.9f),
                    shape = RoundedCornerShape(4.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.ad_label),
                color = Color.White,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}