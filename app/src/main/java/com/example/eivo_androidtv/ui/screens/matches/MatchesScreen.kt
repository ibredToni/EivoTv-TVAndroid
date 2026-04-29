package com.example.eivo_androidtv.ui.screens.matches

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import com.example.eivo_androidtv.R
import com.example.eivo_androidtv.data.remote.EivoApiService
import com.example.eivo_androidtv.data.remote.dto.ComunidadDto
import com.example.eivo_androidtv.data.remote.dto.DeporteDto
import com.example.eivo_androidtv.data.remote.dto.EventoDto
import com.example.eivo_androidtv.ui.components.*
import com.example.eivo_androidtv.utils.formatToTime
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent

private val SpanishLocale = Locale.forLanguageTag("es-ES")

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MatchesScreen(
    initialSport: String,
    selectedRegion: ComunidadDto?,
    onRegionSelected: (ComunidadDto) -> Unit,
    filterClubId: String? = null,
    onNavigateToClubSelection: () -> Unit,
    onNavigateToPlayer: (String, String?) -> Unit,
    onClearClubFilter: () -> Unit,
    onNavigateToProfile: () -> Unit,
    apiService: EivoApiService,
    sports: List<DeporteDto>,
    onSportSelected: (String) -> Unit,
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
) {
    var onlyDirectos by remember { mutableStateOf(false) }
    var selectedDate by remember(initialDate) { mutableStateOf(initialDate) }
    var matches by remember { mutableStateOf<List<EventoDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var currentSportId by remember(initialSport) { mutableStateOf(initialSport) }
    var errorOccurred by remember { mutableStateOf(false) }
    var isOffline by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    var focusedSportName by remember { mutableStateOf<String?>(null) }
    val sportFocusRequesters = remember(sports) { List(sports.size) { FocusRequester() } }

    val initialIndex = remember(sports, initialSport) {
        val index = sports.indexOfFirst { it.id.toString() == initialSport }
        if (index != -1) index else 0
    }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
        sportFocusRequesters[initialIndex].requestFocus()
    }

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
            } catch (e: Exception) {
                isOffline = (e is java.net.UnknownHostException || e is java.net.ConnectException || e is java.io.IOException)
                errorOccurred = true
            } finally {
                isLoading = false
            }
        }
    }

    val actualizarDeporte = { nombre: String ->
        sports.find { it.nombre == nombre }?.let { deporte ->
            val newId = deporte.id.toString()
            if (currentSportId != newId) {
                isLoading = true
                currentSportId = newId
                onSportSelected(newId)
                onlyDirectos = false
            }
        }
    }

    LaunchedEffect(selectedRegion, onlyDirectos, filterClubId, selectedDate, currentSportId) {
        loadMatches()
    }

    LaunchedEffect(focusedSportName) {
        focusedSportName?.let { nombre ->
            kotlinx.coroutines.delay(300)
            actualizarDeporte(nombre)
        }
    }

    val currentSportName = sports.find { it.id.toString() == currentSportId }?.nombre ?: ""

    EivoScreenContainer {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 48.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_eivo_logo),
                    contentDescription = null,
                    modifier = Modifier.width(80.dp)
                )

                ChipGroup(
                    items = sports.map { it.nombre },
                    selectedItem = currentSportName,
                    focusRequesters = sportFocusRequesters,
                    onItemSelected = { nombre -> actualizarDeporte(nombre) },
                    onItemFocused = { nombre -> focusedSportName = nombre },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(0.5f)
                        .onPreviewKeyEvent { keyEvent ->
                            if (keyEvent.key == Key.DirectionUp && keyEvent.type == KeyEventType.KeyDown) {
                                onBackPressedDispatcher?.onBackPressed()
                                true
                            } else {
                                false
                            }
                        }
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
                onDateChange = {
                    selectedDate = it
                    onDateSelected(it)
                }
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
                                    onClick = { onNavigateToPlayer(match.id.toString(), match.img_back) }
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
    val barHeight = 42.dp

    Row(
        modifier = Modifier.fillMaxWidth().height(barHeight),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // BOTÓN IZQUIERDA
        FilterButton(
            text = if (onlyDirectos) stringResource(R.string.matches_exit_live) else stringResource(R.string.matches_view_live),
            isActive = onlyDirectos,
            showLiveDot = !onlyDirectos,
            onClick = onToggleDirectos,
            modifier = Modifier.fillMaxHeight()
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxHeight()
        ) {
            // GRUPO DE DÍAS: Replicamos estructura de FilterButton (Box -> Border -> Padding 4dp)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .onFocusChanged { isDateGroupFocused = it.isFocused }
                    .border(
                        width = 2.dp,
                        color = if (isDateGroupFocused) Color.White else Color.Transparent,
                        shape = RectangleShape
                    )
                    .padding(4.dp) // <--- ESTO es lo que iguala la altura con el FilterButton
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxHeight().background(Color(0xFF151920))
                ) {
                    IconButtonArrow(R.drawable.ic_arrow_left) { onDateChange(selectedDate.minusDays(1)) }
                    DateLabel(date = selectedDate.minusDays(1), isSelected = false)
                    DateLabel(date = selectedDate, isSelected = true)
                    DateLabel(date = selectedDate.plusDays(1), isSelected = false)
                    IconButtonArrow(R.drawable.ic_arrow_right) { onDateChange(selectedDate.plusDays(1)) }
                }
            }

            // GRUPO DE MES: Replicamos estructura
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .onFocusChanged { isMonthGroupFocused = it.isFocused }
                    .border(
                        width = 2.dp,
                        color = if (isMonthGroupFocused) Color.White else Color.Transparent,
                        shape = RectangleShape
                    )
                    .padding(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxHeight().background(Color(0xFF151920))
                ) {
                    IconButtonArrow(R.drawable.ic_arrow_left) { onDateChange(selectedDate.minusMonths(1)) }
                    Box(
                        modifier = Modifier
                            .width(110.dp)
                            .background(Color(0xFF2A2F37))
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = selectedDate.month.getDisplayName(TextStyle.FULL, SpanishLocale).uppercase(SpanishLocale),
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Normal),
                            maxLines = 1
                        )
                    }
                    IconButtonArrow(R.drawable.ic_arrow_right) { onDateChange(selectedDate.plusMonths(1)) }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // BOTÓN DERECHA
        FilterButton(
            text = if (hasClubFilter) stringResource(R.string.matches_view_all_clubs) else stringResource(R.string.matches_choose_club),
            isActive = hasClubFilter,
            onClick = { if (hasClubFilter) onClearClubFilter() else onNavigateToClubSelection() },
            modifier = Modifier.fillMaxHeight()
        )
    }
}

@Composable
fun DateLabel(date: LocalDate, isSelected: Boolean) {
    val today = LocalDate.now()
    val dayName = if (date.isEqual(today)) stringResource(R.string.matches_today)
    else date.dayOfWeek.getDisplayName(TextStyle.SHORT, SpanishLocale).uppercase(SpanishLocale).replace(".", "")

    Box(
        modifier = Modifier
            .width(75.dp)
            .background(color = if (isSelected) Color(0xFF2A2F37) else Color.Transparent)
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$dayName ${date.dayOfMonth}",
            color = if (isSelected) Color.White else Color.Gray,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontSize = 17.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            ),
            maxLines = 1
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun IconButtonArrow(iconRes: Int, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxHeight().aspectRatio(1f),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
        shape = ClickableSurfaceDefaults.shape(RectangleShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color(0xFF424242),
            focusedContainerColor = Color.White
        )
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.requiredSize(40.dp),
                tint = Color.Black
            )
        }
    }
}