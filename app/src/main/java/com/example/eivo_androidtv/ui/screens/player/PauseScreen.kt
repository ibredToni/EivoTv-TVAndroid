package com.example.eivo_androidtv.ui.screens.player

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
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.example.eivo_androidtv.R
import com.example.eivo_androidtv.data.remote.dto.AdDto
import com.example.eivo_androidtv.data.remote.dto.EventoDetalleDto
import com.example.eivo_androidtv.ui.components.BackgroundFoci

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PauseScreen(
    onResume: () -> Unit,
    detalle: EventoDetalleDto,
    ads: List<AdDto>
) {

    val pauseAd = ads.find { it.position == "pause" }

    val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
        focusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(0.32f)
                    .fillMaxHeight()
                    .background(Color(0xFF070A0D))
            )

            Box(modifier = Modifier.weight(0.68f).fillMaxHeight()) {
                AsyncImage(
                    model = pauseAd?.url,
                    contentDescription = "Publicidad",
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    contentScale = ContentScale.FillBounds,
                    error = null
                )

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.2f)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF070A0D), Color.Transparent)
                            )
                        )
                )
            }
        }

        BackgroundFoci(Color(0xFF269091))

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.28f)
                .padding(start = 50.dp, top = 40.dp, bottom = 40.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.dp, Color.White.copy(alpha = 0.5f), RectangleShape)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.portero),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        android.util.Log.d("PAUSE_DEBUG", "Loc: ${detalle.img_loc} | Vis: ${detalle.img_vis}")
                        AsyncImage(
                            model = coil.request.ImageRequest.Builder(LocalContext.current)
                                .data(detalle.img_loc)
                                .crossfade(true)
                                .listener(
                                    onError = { _, result ->
                                        android.util.Log.e("COIL_ERROR", "Error Loc: ${result.throwable.message}")
                                    }
                                )
                                .build(),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            contentScale = ContentScale.Fit,
                            error = painterResource(id = R.drawable.escudo_1)
                        )
                        Spacer(Modifier.width(8.dp))
                        AsyncImage(
                            model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                .data(detalle.img_vis)
                                .crossfade(true)
                                .listener(
                                    onError = { _, result ->
                                        android.util.Log.e("COIL_ERROR", "Error Loc: ${result.throwable.message}")
                                    }
                                )
                                .build(),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            contentScale = ContentScale.Fit,
                            error = painterResource(id = R.drawable.escudo_2)
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = detalle.nombre?.uppercase() ?: "SIN NOMBRE",
                        color = Color.White,
                        fontSize = 18.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = detalle.descripcion ?: "",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 10.sp)
                    Text(
                        text = detalle.inicio ?: "",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 9.sp)

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onResume,
                        shape = ButtonDefaults.shape(RectangleShape),
                        colors = ButtonDefaults.colors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .height(22.dp)
                            .wrapContentWidth()
                            .focusRequester(focusRequester),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_play),
                                contentDescription = null,
                                modifier = Modifier.size(8.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Seguir viendo",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

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
                text = "Ad",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}