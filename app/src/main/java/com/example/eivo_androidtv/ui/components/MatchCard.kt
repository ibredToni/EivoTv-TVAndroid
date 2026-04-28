package com.example.eivo_androidtv.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.eivo_androidtv.R
import com.example.eivo_androidtv.data.remote.dto.EventoDto
import com.example.eivo_androidtv.utils.formatToTime

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MatchCard(
    match: EventoDto,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    var isFocused by remember { mutableStateOf(false) }
    val equipos = match.nombre.split(" - ")
    val nombreLocal = equipos.getOrNull(0) ?: "Local"
    val nombreVisitante = equipos.getOrNull(1) ?: "Visitante"

    Surface(
        onClick = onClick,
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
        shape = ClickableSurfaceDefaults.shape(RectangleShape),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(BorderStroke(3.dp, Color.White))
        ),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
            focusedContainerColor = Color(0xFF151920)
        ),
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.isFocused }
            .padding(4.dp)
    ) {
        Column(
            modifier = Modifier
                .background(if (isFocused) Color.Black else Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(if (isFocused) 1.1f else 1.6f)
            ) {
                // Fondo de la Card
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(match.img_back)
                        .fallback(R.drawable.back_error)
                        .error(R.drawable.back_error)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    alpha = if (isFocused) 0.2f else 1f
                )

                // Contenedor de logos centrales
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        AsyncImage(
                            model = match.img_loc,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().scale(1.8f),
                            contentScale = ContentScale.Fit,
                            // AJUSTADO: alpha igual que el fondo (0.2f)
                            alpha = if (isFocused) 0.2f else 1f
                        )
                    }

                    Spacer(modifier = Modifier.width(30.dp))

                    Box(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        AsyncImage(
                            model = match.img_vis,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().scale(1.8f),
                            contentScale = ContentScale.Fit,
                            // AJUSTADO: alpha igual que el fondo (0.2f)
                            alpha = if (isFocused) 0.2f else 1f
                        )
                    }
                }

                // Información SUPERPUESTA
                if (isFocused) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "${nombreLocal.uppercase()}\n${nombreVisitante.uppercase()}",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = match.descripcion,
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = formatToTime(match.inicio),
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                        )
                    }
                }
            }

            // Información INFERIOR
            if (!isFocused) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp)
                ) {
                    Text(
                        text = match.descripcion,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatToTime(match.inicio),
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}