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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.focus.onFocusChanged
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import com.example.eivo_androidtv.data.remote.EivoApiService
import com.example.eivo_androidtv.data.remote.dto.ComunidadDto
import com.example.eivo_androidtv.data.remote.dto.DeporteDto
import com.example.eivo_androidtv.data.remote.dto.EventoDto
import com.example.eivo_androidtv.ui.components.EivoScreenContainer
import com.example.eivo_androidtv.ui.components.ErrorStateComponent
import com.example.eivo_androidtv.ui.components.FilterButton
import com.example.eivo_androidtv.ui.components.NoContentState
import kotlinx.coroutines.launch

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
    var errorOccurred by remember { mutableStateOf(false) }
    var isOffline by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val loadMatches = suspend {
        if (selectedRegion != null) {
            isLoading = true
            errorOccurred = false
            isOffline = false

            try {
                val communityId = selectedRegion.id.toString()
                matches = if (onlyDirectos) {
                    apiService.fetchDirectos(communityId, currentSportId, filterClubId)
                } else {
                    apiService.fetchEventos(communityId, currentSportId, selectedDate.toString(), filterClubId)
                }
            } catch (e: java.net.UnknownHostException) {
                isOffline = true
                errorOccurred = true
            } catch (e: Exception) {
                errorOccurred = true
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(selectedRegion, onlyDirectos, filterClubId, selectedDate, currentSportId) {
        loadMatches()
    }

    val currentSportName = sports.find { it.id.toString() == currentSportId }?.nombre ?: ""

    EivoScreenContainer {

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

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when {
                    isLoading -> {
                        Box(Modifier.fillMaxSize(), Alignment.Center) {
                            androidx.compose.material.CircularProgressIndicator(color = Color.White)
                        }
                    }
                    errorOccurred -> {
                        ErrorStateComponent(
                            isOffline = isOffline,
                            onRetry = { scope.launch { loadMatches() } }
                        )
                    }
                    matches.isEmpty() -> {
                        val emptyMessage = when {
                            onlyDirectos -> stringResource(
                                R.string.no_matches_live,
                                selectedRegion?.name ?: "",
                                currentSportName

                            )
                            filterClubId != null -> stringResource(R.string.no_matches_club)
                            else -> stringResource(R.string.no_matches_available)
                        }
                        NoContentState(message = emptyMessage)
                    }
                    else -> {
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
                                    onClick = { onNavigateToPlayer(match.id.toString()) }
                                )
                            }
                        }
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
            text = if (onlyDirectos) stringResource(R.string.matches_exit_live) else stringResource(R.string.matches_view_live),
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
                        Text(text = selectedDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault()).uppercase(), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButtonArrow(R.drawable.ic_arrow_right) { onDateChange(selectedDate.plusMonths(1)) }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        FilterButton(
            text = if (hasClubFilter) stringResource(R.string.matches_view_all_clubs) else stringResource(R.string.matches_choose_club),
            isActive = hasClubFilter,
            onClick = { if (hasClubFilter) onClearClubFilter() else onNavigateToClubSelection() }
        )
    }
}
@Composable
fun DateLabel(date: LocalDate, isSelected: Boolean) {
    val today = LocalDate.now()

    val dayName = when {
        date.isEqual(today) -> stringResource(R.string.matches_today)
        else -> date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
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