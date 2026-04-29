package com.example.eivo_androidtv.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.eivo_androidtv.data.remote.dto.ComunidadDto
import androidx.compose.foundation.lazy.items

@Composable
fun RegionChipGroup(
    regions: List<ComunidadDto>,
    selectedRegion: ComunidadDto?,
    onRegionSelected: (ComunidadDto) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(), // Ocupa todo el ancho
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        regions.forEach { region ->
            Box(modifier = Modifier.padding(horizontal = 6.dp)) {
                RegionSelectableChip(
                    region = region,
                    isSelected = region.id == selectedRegion?.id,
                    onClick = { onRegionSelected(region) },
                    onFocus = { onRegionSelected(region) }
                )
            }
        }
    }
}