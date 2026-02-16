package com.example.eivo_androidtv.ui.screens.player

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import com.example.eivo_androidtv.R
import com.example.eivo_androidtv.ui.components.BackgroundFoci

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PauseScreen(onResume: () -> Unit) {
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
                Image(
                    painter = painterResource(id = R.drawable.philadelphia),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
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
                        Image(painterResource(id = R.drawable.escudo_1), null, Modifier.size(40.dp))
                        Spacer(Modifier.width(8.dp))
                        Image(painterResource(id = R.drawable.escudo_2), null, Modifier.size(40.dp))
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = "FORMENTERA\nvs ARTÀ",
                        color = Color.White,
                        fontSize = 18.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(Modifier.height(4.dp))

                    Text("Amateur Reg. Pref.", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                    Text("Domingo 2 de nov. '25", color = Color.White.copy(alpha = 0.7f), fontSize = 9.sp)

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
                            .wrapContentWidth(),
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