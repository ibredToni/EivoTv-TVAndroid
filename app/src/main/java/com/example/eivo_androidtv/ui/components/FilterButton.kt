package com.example.eivo_androidtv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun FilterButton(
    text: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showLiveDot: Boolean = false,
    fontSize: TextUnit = 14.sp
) {
    var isFocused by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .onFocusChanged { isFocused = it.isFocused }
            .border(
                width = 2.dp,
                color = if (isFocused) Color.White else Color.Transparent,
                shape = RectangleShape
            )
            .padding(4.dp)
    ) {
        Surface(
            onClick = onClick,
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
            shape = ClickableSurfaceDefaults.shape(RectangleShape),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = if (isActive) Color(0xFFC0C0C0) else Color(0xFF151920),
                focusedContainerColor = if (isActive) Color(0xFFC0C0C0) else Color(0xFF151920)
            ),
            modifier = Modifier
                .width(180.dp)
                .fillMaxHeight()
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (showLiveDot) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color.Red, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }

                Text(
                    text = text,
                    color = if (isActive) Color.Black else Color.White,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = fontSize,
                        fontWeight = FontWeight.Normal
                    )
                )
            }
        }
    }
}