package com.eivotv.androidtv.ui.screens.club

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
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.tv.material3.*
import com.eivotv.androidtv.R
import com.eivotv.androidtv.data.remote.EivoApiService
import com.eivotv.androidtv.data.remote.dto.ClubDto
import com.eivotv.androidtv.data.remote.dto.ComunidadDto
import com.eivotv.androidtv.ui.components.ProfileIcon
import com.eivotv.androidtv.ui.components.BackgroundFoci
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.eivotv.androidtv.data.remote.dto.DeporteDto
import com.eivotv.androidtv.ui.components.ChipGroup
import com.eivotv.androidtv.ui.components.ErrorStateComponent
import com.eivotv.androidtv.ui.components.NoContentState
import kotlinx.coroutines.launch

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ClubSelectionScreen(
    selectedRegion: ComunidadDto?,
    selectedSportId: String,
    apiService: EivoApiService,
    onClubSelected: (String) -> Unit,
    sports: List<DeporteDto>,
    onSportSelected: (String) -> Unit,
    onBackWithoutSelection: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    var clubs by remember { mutableStateOf<List<ClubDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorOccurred by remember { mutableStateOf(false) }
    var isOffline by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val loadClubs = suspend {
        if (selectedRegion != null && selectedSportId.isNotEmpty()) {
            isLoading = true
            errorOccurred = false
            isOffline = false
            try {
                clubs = apiService.fetchClubs(selectedRegion.id.toString(), selectedSportId)
            } catch (e: Exception) {
                isOffline = (e is java.net.UnknownHostException || e is java.net.ConnectException || e is java.io.IOException)
                errorOccurred = true
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(selectedRegion, selectedSportId) {
        loadClubs()
    }

    val currentSportName = sports.find { it.id.toString() == selectedSportId }?.nombre ?: ""

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        BackgroundFoci(MaterialTheme.colorScheme.primary)

        when {
            isLoading -> {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    androidx.compose.material.CircularProgressIndicator(color = Color.White)
                }
            }

            errorOccurred -> {
                ErrorStateComponent(
                    isOffline = isOffline,
                    onRetry = { scope.launch { loadClubs() } }
                )
            }

            clubs.isEmpty() -> {
                NoContentState(message = stringResource(
                    R.string.no_clubs_available,
                    selectedRegion?.name ?: ""
                ))
            }

            else -> {
                Column(modifier = Modifier.fillMaxSize().padding(horizontal = 48.dp)) {
                    // Header
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
                                sports.find { it.nombre == nombre }?.let { onSportSelected(it.id.toString()) }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        ProfileIcon(onNavigateToProfile = onNavigateToProfile)
                    }

                    // Botón Volver
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        var isBackFocused by remember { mutableStateOf(false) }
                        Box(
                            modifier = Modifier
                                .onFocusChanged { isBackFocused = it.isFocused }
                                .border(
                                    width = 2.dp,
                                    color = if (isBackFocused) Color.White else Color.Transparent,
                                    shape = RectangleShape
                                )
                                .padding(4.dp)
                        ) {
                            Surface(
                                onClick = onBackWithoutSelection,
                                scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
                                shape = ClickableSurfaceDefaults.shape(RectangleShape),
                                colors = ClickableSurfaceDefaults.colors(
                                    containerColor = Color(0xFF202636),
                                    focusedContainerColor = Color(0xFF202636)
                                )
                            ) {
                                Text(
                                    text = stringResource(R.string.back_without_club),
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalArrangement = Arrangement.spacedBy(40.dp),
                        contentPadding = PaddingValues(top = 20.dp, bottom = 48.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(clubs) { club ->
                            ClubSelectionItem(
                                name = club.name,
                                imageUrl = club.image,
                                fallbackUrl = club.fail,
                                onClick = { onClubSelected(club.id.toString()) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun ClubSelectionItem(
    name: String,
    imageUrl: String,
    fallbackUrl: String,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    var urlToLoad by remember(imageUrl) { mutableStateOf(imageUrl) }

    Column(
        modifier = Modifier
            .width(140.dp)
            .zIndex(if (isFocused) 1f else 0f),
        horizontalAlignment = Alignment.Start
    ) {
        Surface(
            onClick = onClick,
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
            shape = ClickableSurfaceDefaults.shape(RectangleShape),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .onFocusChanged { isFocused = it.isFocused }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 2.dp,
                        color = if (isFocused) Color.White else Color.Transparent,
                        shape = RectangleShape
                    )
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(urlToLoad)
                        .crossfade(true)
                        .build(),
                    contentDescription = name,
                    onError = {
                        if (urlToLoad != fallbackUrl) urlToLoad = fallbackUrl
                    },
                    placeholder = ColorPainter(Color.Black),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = name.uppercase(),
            color = Color.White,
            style = MaterialTheme.typography.titleSmall.copy(fontSize = 12.sp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp)
        )
    }
}