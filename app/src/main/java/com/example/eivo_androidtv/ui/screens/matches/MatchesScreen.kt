package com.example.eivo_androidtv.ui.screens.matches

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.tv.material3.ExperimentalTvMaterial3Api

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.example.eivo_androidtv.R
import com.example.eivo_androidtv.ui.components.ChipGroup
import com.example.eivo_androidtv.ui.components.MatchCard
import com.example.eivo_androidtv.ui.components.ProfileIcon
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.onFocusChanged
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import com.example.eivo_androidtv.data.network.MockApiService
import com.example.eivo_androidtv.data.remote.EivoApiService
import com.example.eivo_androidtv.data.remote.dto.ComunidadDto
import com.example.eivo_androidtv.data.remote.dto.DeporteDto
import com.example.eivo_androidtv.data.remote.dto.EventoDto
import com.example.eivo_androidtv.ui.components.BackgroundFoci
import com.example.eivo_androidtv.ui.components.FilterButton

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MatchesScreen(
    initialSport: String,
    selectedRegion: ComunidadDto?,
    onRegionSelected: (ComunidadDto) -> Unit,
    filterClubId: String? = null,
    onNavigateToClubSelection: () -> Unit,
    onNavigateToPlayer: (String) -> Unit,
    onClearClubFilter: () -> Unit,
    onNavigateToProfile: () -> Unit,
    apiService: EivoApiService,
    sports: List<DeporteDto>,
    onSportSelected: (String) -> Unit
) {
    var onlyDirectos by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var matches by remember { mutableStateOf<List<EventoDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var currentSportId by remember(initialSport) { mutableStateOf(initialSport) }

    LaunchedEffect(selectedRegion, initialSport, onlyDirectos, filterClubId, selectedDate, currentSportId) {
        if (selectedRegion == null) return@LaunchedEffect

        isLoading = true
        try {
            val communityId = selectedRegion.id.toString()

            val result = if (onlyDirectos) {
                apiService.fetchDirectos(communityId, initialSport, filterClubId)
            } else {
                val formattedDate = selectedDate.toString()
                apiService.fetchEventos(communityId, initialSport, formattedDate, filterClubId)
            }
            matches = result
        } catch (e: Exception) {
            android.util.Log.e("MATCHES_ERROR", "Fallo al cargar partidos: ${e.message}")
            matches = emptyList()
        } finally {
            isLoading = false
        }
    }

    val currentSportName = sports.find { it.id.toString() == currentSportId }?.nombre ?: ""

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        BackgroundFoci(Color(0xFF269091))

        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 48.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Image(painterResource(id = R.drawable.ic_eivo_logo), null, Modifier.width(80.dp))

                ChipGroup(
                    items = sports.map { it.nombre },
                    selectedItem = currentSportName,
                    onItemSelected = { nombre ->
                        sports.find { it.nombre == nombre }?.let { deporte ->
                            val newId = deporte.id.toString()
                            currentSportId = newId
                            onSportSelected(newId)
                            onlyDirectos = false
                        }
                    },
                    modifier = Modifier.weight(1f).fillMaxWidth(0.5f)
                )

                ProfileIcon(onNavigateToProfile = onNavigateToProfile)
            }

            FilterSecondaryBar(
                onNavigateToClubSelection = onNavigateToClubSelection,
                filterClub = filterClubId,
                onClearClubFilter = onClearClubFilter,
                onlyDirectos = onlyDirectos,
                onToggleDirectos = { onlyDirectos = !onlyDirectos },
                selectedDate = selectedDate,
                onDateChange = { selectedDate = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    androidx.compose.material.CircularProgressIndicator(color = Color.White)
                }
            } else if (matches.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val emptyMessage = when {
                        onlyDirectos -> "No hay partidos en directo en ${selectedRegion?.name} para $currentSportName"
                        filterClubId != null -> "Este club no tiene partidos programados"
                        else -> "No hay partidos disponibles"
                    }

                    Text(
                        text = emptyMessage.uppercase(),
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(32.dp),
                    contentPadding = PaddingValues(bottom = 48.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(matches) { match ->
                        MatchCard(
                            match = match,
                            onClick = {
                                onNavigateToPlayer(match.id.toString())
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun FilterSecondaryBar(
    onNavigateToClubSelection: () -> Unit,
    filterClub: String?,
    onClearClubFilter: () -> Unit,
    onlyDirectos: Boolean,
    onToggleDirectos: () -> Unit,
    selectedDate: LocalDate,
    onDateChange: (LocalDate) -> Unit
) {
    val hasClubFilter = filterClub != null
    var isDateGroupFocused by remember { mutableStateOf(false) }
    var isMonthGroupFocused by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterButton(
            text = if (onlyDirectos) "Salir de directos" else "Ver directos",
            isActive = onlyDirectos,
            showLiveDot = !onlyDirectos,
            onClick = onToggleDirectos
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .onFocusChanged { isDateGroupFocused = it.hasFocus }
                    .border(
                        width = 2.dp,
                        color = if (isDateGroupFocused) Color.White else Color.Transparent,
                        shape = RectangleShape
                    )
                    .padding(5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(Color(0xFF151920))) {
                    IconButtonArrow(R.drawable.ic_arrow_left) { onDateChange(selectedDate.minusDays(1)) }
                    DateLabel(date = selectedDate.minusDays(1), isSelected = false)
                    DateLabel(date = selectedDate, isSelected = true)
                    DateLabel(date = selectedDate.plusDays(1), isSelected = false)
                    IconButtonArrow(R.drawable.ic_arrow_right) { onDateChange(selectedDate.plusDays(1)) }
                }
            }
            Box(
                modifier = Modifier
                    .onFocusChanged { isMonthGroupFocused = it.hasFocus }
                    .border(
                        width = 2.dp,
                        color = if (isMonthGroupFocused) Color.White else Color.Transparent,
                        shape = RectangleShape
                    )
                    .padding(5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(Color(0xFF151920))) {
                    IconButtonArrow(R.drawable.ic_arrow_left) { onDateChange(selectedDate.minusMonths(1)) }
                    Box(modifier = Modifier.width(110.dp).background(Color(0xFF2A2F37)).padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                        Text(text = selectedDate.month.getDisplayName(TextStyle.FULL, Locale("es", "ES")).uppercase(), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButtonArrow(R.drawable.ic_arrow_right) { onDateChange(selectedDate.plusMonths(1)) }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        FilterButton(
            text = if (hasClubFilter) "Ver todos los clubes" else "Escoge tu club",
            isActive = hasClubFilter,
            onClick = { if (hasClubFilter) onClearClubFilter() else onNavigateToClubSelection() }
        )
    }
}
@Composable
fun DateLabel(date: LocalDate, isSelected: Boolean) {
    val today = LocalDate.now()

    val dayName = when {
        date.isEqual(today) -> "HOY"
        else -> date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("es", "ES"))
            .uppercase().replace(".", "")
    }

    val label = "$dayName ${date.dayOfMonth}"

    Box(
        modifier = Modifier
            .width(75.dp)
            .background(color = if (isSelected) Color(0xFF2A2F37) else Color.Transparent)
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else Color.Gray,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun IconButtonArrow(iconRes: Int, onClick: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }
    Surface(
        onClick = onClick,
        modifier = Modifier.size(32.dp),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
        shape = ClickableSurfaceDefaults.shape(RectangleShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color(0xFFC0C0C0),
            focusedContainerColor = Color.White
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize().onFocusChanged { isFocused = it.isFocused },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = Color.Black
            )
        }
    }
}