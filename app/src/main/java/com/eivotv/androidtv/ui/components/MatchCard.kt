package com.eivotv.androidtv.ui.components

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
import com.eivotv.androidtv.R
import com.eivotv.androidtv.data.remote.dto.EventoDto
import com.eivotv.androidtv.utils.formatToTime

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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 3.dp)
    ) {
        Surface(
            onClick = onClick,
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
            shape = ClickableSurfaceDefaults.shape(RectangleShape),
            border = ClickableSurfaceDefaults.border(
                focusedBorder = Border(BorderStroke(3.dp, Color.White))
            ),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = Color.Transparent,
                focusedContainerColor = Color.Black
            ),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { isFocused = it.isFocused }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (isFocused) Modifier.matchParentSize()
                            else Modifier.aspectRatio(16f / 9f)
                        ),
                    alpha = if (isFocused) 0.3f else 1f
                )

                // Contenedor de logos centrales
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (isFocused) Modifier.matchParentSize()
                            else Modifier.aspectRatio(16f / 9f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = match.img_loc,
                            contentDescription = null,
                            modifier = Modifier.weight(0.5f).fillMaxHeight(0.6f),
                            contentScale = ContentScale.Fit,
                            alpha = if (isFocused) 0.3f else 1f
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        AsyncImage(
                            model = match.img_vis,
                            contentDescription = null,
                            modifier = Modifier.weight(0.5f).fillMaxHeight(0.6f),
                            contentScale = ContentScale.Fit,
                            alpha = if (isFocused) 0.3f else 1f
                        )
                    }
                }

                // Información SUPERPUESTA
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)) {
                        if (isFocused) {
                            Text(
                                text = "${nombreLocal.uppercase()}\n${nombreVisitante.uppercase()}",
                                color = Color.White,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 16.sp
                                ),
                                modifier = Modifier.padding(8.dp).align(Alignment.TopStart)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = match.descripcion,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = formatToTime(match.inicio),
                            color = Color.White.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                        )
                    }
                }
            }
        }
    }
}