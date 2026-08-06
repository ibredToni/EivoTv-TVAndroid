package com.eivotv.androidtv.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.tv.material3.MaterialTheme
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.eivotv.androidtv.data.remote.dto.ComunidadDto
import com.eivotv.androidtv.R

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun RegionSelectableChip(
    region: ComunidadDto,
    isSelected: Boolean,
    onClick: () -> Unit,
    onFocus: () -> Unit = {}
) {
    val flagRes = when {
        region.name.contains("Baleares", ignoreCase = true) -> R.drawable.baleares
        region.name.contains("Madrid", ignoreCase = true) -> R.drawable.madrid
        region.name.contains("Valencia", ignoreCase = true) -> R.drawable.valencia
        else -> null
    }

    val shortName = when {
        region.name.contains("Baleares", true) -> "IB"
        region.name.contains("Madrid", true) -> "MAD"
        region.name.contains("Valencia", true) -> "VAL"
        else -> region.name.take(3).uppercase()
    }

    val selectedColor = Color(0xFFA2A7B5)
    val defaultColor = Color(0xFF202636)

    var isFocused by remember { mutableStateOf(false) }

    val backgroundColor = if (isFocused) {
        defaultColor
    } else if (isSelected) {
        selectedColor
    } else {
        defaultColor
    }

    Surface(
        onClick = onClick,
        modifier = Modifier
            .width(85.dp)
            .height(28.dp)
            .onFocusChanged {
            if (it.isFocused) {
                onFocus()
            }
        },
        shape = ClickableSurfaceDefaults.shape(CircleShape),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = backgroundColor,
            focusedContainerColor = defaultColor,
            pressedContainerColor = defaultColor
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(2.dp, Color.White),
                shape = CircleShape
            ),
            border = Border.None
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            if (flagRes != null) {
                Image(
                    painter = painterResource(id = flagRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(width = 38.dp, height = 28.dp)
                        .clip(RectangleShape),
                    contentScale = ContentScale.Crop
                )
            }
            Text(
                text = shortName,
                modifier = Modifier.padding(start = 6.dp),
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
                maxLines = 1
            )
        }
    }
}