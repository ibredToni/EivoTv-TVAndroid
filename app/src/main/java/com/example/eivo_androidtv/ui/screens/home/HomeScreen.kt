package com.example.eivo_androidtv.ui.screens.home

import android.view.KeyEvent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.eivo_androidtv.R
import com.example.eivo_androidtv.data.local.SessionManager
import com.example.eivo_androidtv.data.remote.EivoApiService
import com.example.eivo_androidtv.data.remote.dto.AdDto
import com.example.eivo_androidtv.data.remote.dto.ComunidadDto
import com.example.eivo_androidtv.data.remote.dto.DeporteDto
import com.example.eivo_androidtv.data.remote.dto.DestacadoDto
import com.example.eivo_androidtv.ui.components.ChipGroup
import com.example.eivo_androidtv.ui.components.ProfileIcon
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.focusable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import com.example.eivo_androidtv.ui.components.ConfirmationDialog
import com.example.eivo_androidtv.ui.components.EivoScreenContainer
import com.example.eivo_androidtv.ui.components.ErrorStateComponent
import com.example.eivo_androidtv.ui.components.NoContentState
import com.example.eivo_androidtv.ui.components.RegionChipGroup
import com.example.eivo_androidtv.utils.formatToLongDate
import kotlinx.coroutines.launch

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    apiService: EivoApiService,
    selectedRegion: ComunidadDto?,
    onRegionSelected: (ComunidadDto) -> Unit,
    sports: List<DeporteDto>,
    selectedSportId: String,
    onSportSelected: (String) -> Unit,
    onNavigateToMatches: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToPlayer: (String, String?) -> Unit,
    regions: List<ComunidadDto>,
    sessionManager: SessionManager,
    isInitialLoading: Boolean,
) {
    var destacados by remember { mutableStateOf<List<DestacadoDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var adsHome by remember { mutableStateOf<List<AdDto>>(emptyList()) }
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { destacados.size })
    val topBarFocusRequester = remember { FocusRequester() }
    val sportsChipFocusRequester = remember { FocusRequester() }
    val selectedSportName = sports.find { it.id.toString() == selectedSportId }?.nombre ?: ""
    val token = remember { sessionManager.fetchAuthToken() ?: "" }
    var errorOccurred by remember { mutableStateOf(false) }
    var isOffline by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var showExitDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    BackHandler(enabled = !showExitDialog) {
        showExitDialog = true
    }

    val loadData = suspend {
        isLoading = true
        errorOccurred = false
        isOffline = false
        try {
            destacados = apiService.fetchDestacados()
            val adsDeferred = apiService.fetchAdverts()
            val posicionesValidas = listOf("home_1", "home_2")
            adsHome = adsDeferred.filter { it.position in posicionesValidas }
        } catch (e: Exception) {
            isOffline = (e is java.net.UnknownHostException || e is java.net.ConnectException || e is java.io.IOException)
            errorOccurred = true
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    EivoScreenContainer {
        when {
            isLoading || isInitialLoading -> {
                Box(Modifier.fillMaxSize().focusable().focusRequester(remember { FocusRequester() }), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            }

            errorOccurred -> {
                ErrorStateComponent(
                    isOffline = isOffline,
                    onRetry = { scope.launch { loadData() } }
                )
            }

            regions.isEmpty() -> {
                NoContentState(stringResource(R.string.no_regions_available))
            }

            else -> {
                LaunchedEffect(regions) {
                    if (regions.isNotEmpty()) {
                        kotlinx.coroutines.yield()
                        topBarFocusRequester.requestFocus()
                    }
                }

                Column(modifier = Modifier.fillMaxSize().padding(horizontal = 48.dp, vertical = 24.dp)) {
                    Box(modifier = Modifier.focusRequester(topBarFocusRequester)) {
                        HomeTopBar(
                            regions = regions,
                            selectedRegion = selectedRegion,
                            onRegionSelected = onRegionSelected,
                            onNavigateToProfile = onNavigateToProfile
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        if (destacados.isNotEmpty()) {
                            HeroBanner(
                                banners = destacados,
                                pagerState = pagerState,
                                modifier = Modifier.weight(0.70f).fillMaxHeight(),
                                onBannerClick = {
                                    val currentDestacado = destacados[pagerState.currentPage]
                                    onNavigateToPlayer(currentDestacado.id.toString(), currentDestacado.img)
                                },
                                onMoveDown = { sportsChipFocusRequester.requestFocus() },
                                onMoveUp = { topBarFocusRequester.requestFocus() },
                                token = token
                            )

                            Spacer(modifier = Modifier.width(5.dp))

                            RightSidebar(
                                destacados = destacados,
                                ads = adsHome,
                                currentPagerIndex = pagerState.currentPage,
                                modifier = Modifier.weight(0.25f).fillMaxHeight(),
                                token = token,
                                onNextClick = { eventId ->
                                    val destacado = destacados.find { it.id.toString() == eventId }
                                    onNavigateToPlayer(eventId, destacado?.img)
                                }
                            )
                        } else if (!isLoading) {
                            Box(Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                                Text(stringResource(R.string.no_featured_content), color = Color.Gray)
                            }
                        }
                    }

                    Box(modifier = Modifier.focusRequester(sportsChipFocusRequester)) {
                        ChipGroup(
                            items = sports.map { it.nombre },
                            selectedItem = selectedSportName,
                            onItemSelected = { nombre ->
                                sports.find { it.nombre == nombre }?.let {
                                    onSportSelected(it.id.toString())
                                    onNavigateToMatches(it.id.toString())
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
                        )
                    }
                }
            }
        }

        if (showExitDialog) {
            ConfirmationDialog(
                title = stringResource(R.string.confirmation_exit),
                confirmText = stringResource(R.string.btn_exit),
                dismissText = stringResource(R.string.btn_cancel),
                onConfirm = { (context as? android.app.Activity)?.finish() },
                onDismiss = { showExitDialog = false }
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HomeTopBar(
    regions: List<ComunidadDto>,
    selectedRegion: ComunidadDto?,
    onRegionSelected: (ComunidadDto) -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_eivo_logo),
            contentDescription = null,
            modifier = Modifier.width(90.dp),
            contentScale = ContentScale.Fit
        )

        RegionChipGroup(
            regions = regions,
            selectedRegion = selectedRegion,
            onRegionSelected = onRegionSelected,
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
        )

        ProfileIcon(onNavigateToProfile = onNavigateToProfile)
    }
}

@OptIn(ExperimentalTvMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun HeroBanner(
    banners: List<DestacadoDto>,
    pagerState: PagerState,
    modifier: Modifier,
    onBannerClick: () -> Unit,
    onMoveDown: () -> Unit,
    onMoveUp: () -> Unit,
    token: String = "",
) {
    Box(modifier = modifier.fillMaxHeight()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1,
            userScrollEnabled = false
        ) { page ->
            val data = banners[page]
            var isPageFocused by remember { mutableStateOf(false) }

            Surface(
                modifier = Modifier.fillMaxSize().padding(1.dp),
                shape = RectangleShape,
                border = Border(
                    border = BorderStroke(
                        width = if (isPageFocused) 3.dp else 0.dp,
                        color = if (isPageFocused) Color.White else Color.Transparent
                    ),
                    shape = RectangleShape
                ),
                colors = SurfaceDefaults.colors(containerColor = Color.Black)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(data.img.trim())
                            .crossfade(true)
                            .allowHardware(false)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().background(Color.Black),
                        contentScale = ContentScale.Crop
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f)),
                                    startY = 400f
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(32.dp)
                            .fillMaxWidth(0.7f)
                    ) {
                        // Título Destacado
                        Text(
                            text = data.nombre.uppercase(),
                            color = Color.White,
                            style = MaterialTheme.typography.displayMedium.copy(fontSize = 38.sp, lineHeight = 42.sp),
                            maxLines = 3,
                            overflow = Ellipsis
                        )
                        // Descripción
                        Text(
                            text = data.descripcion,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 18.sp)
                        )
                        // Fecha
                        Text(
                            text = formatToLongDate(data.inicio),
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 18.sp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = onBannerClick,
                            modifier = Modifier
                                .height(26.dp)
                                .wrapContentWidth()
                                .onFocusChanged { state -> isPageFocused = state.isFocused }
                                .onKeyEvent { event ->
                                    if (event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                                        when (event.nativeKeyEvent.keyCode) {
                                            KeyEvent.KEYCODE_DPAD_DOWN -> { onMoveDown(); true }
                                            KeyEvent.KEYCODE_DPAD_UP -> { onMoveUp(); true }
                                            else -> false
                                        }
                                    } else false
                                },
                            shape = ButtonDefaults.shape(RectangleShape),
                            colors = ButtonDefaults.colors(
                                containerColor = Color.White,
                                contentColor = Color.Black,
                                focusedContainerColor = Color.White,
                                focusedContentColor = Color.Black
                            ),
                            scale = ButtonDefaults.scale(focusedScale = 1.1f),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_play),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(2.dp))
                                Text(
                                    text = stringResource(R.string.watch_now),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp)
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        // Paginador (Dots)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val totalDots = banners.size
                            val maxVisibleDots = 5
                            val startDot = when {
                                totalDots <= maxVisibleDots -> 0
                                pagerState.currentPage < 3 -> 0
                                pagerState.currentPage > totalDots - 3 -> totalDots - maxVisibleDots
                                else -> pagerState.currentPage - 2
                            }
                            val endDot = (startDot + maxVisibleDots).coerceAtMost(totalDots)

                            for (index in startDot until endDot) {
                                val isSelected = pagerState.currentPage == index
                                val isEdge = (index == startDot && startDot > 0) || (index == endDot - 1 && endDot < totalDots)
                                val dotWidth = when {
                                    isSelected -> 10.dp
                                    isEdge -> 3.dp
                                    else -> 4.dp
                                }
                                Box(
                                    modifier = Modifier
                                        .height(if (isEdge && !isSelected) 3.dp else 4.dp)
                                        .width(dotWidth)
                                        .background(
                                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.4f),
                                            shape = CircleShape
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun RightSidebar(
    destacados: List<DestacadoDto>,
    ads: List<AdDto>,
    currentPagerIndex: Int,
    modifier: Modifier,
    token: String = "",
    onNextClick: (String) -> Unit
) {
    val context = LocalContext.current
    val gifImageLoader = remember {
        coil.ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= 28) add(ImageDecoderDecoder.Factory()) else add(GifDecoder.Factory())
            }
            .build()
    }

    val nextDestacadoIndex = (currentPagerIndex + 1) % destacados.size
    val nextDestacado = destacados[nextDestacadoIndex]
    val ad1ToShow = remember(ads) { ads.filter { it.position == "home_1" }.let { if (it.isNotEmpty()) it.random() else null } }
    val ad2ToShow = remember(ads) { ads.filter { it.position == "home_2" }.let { if (it.isNotEmpty()) it.random() else null } }

    Column(modifier = modifier.fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SidebarItem(
            imageUrl = nextDestacado.img,
            modifier = Modifier.weight(1.2f),
            token = token,
            imageLoader = gifImageLoader,
            isClickable = true,
            onClick = { onNextClick(nextDestacado.id.toString()) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        SidebarItem(
            imageUrl = ad1ToShow?.url,
            modifier = Modifier.weight(.9f).fillMaxSize(),
            imageLoader = gifImageLoader,
            isClickable = false
        )

        SidebarItem(
            imageUrl = ad2ToShow?.url,
            modifier = Modifier.weight(.9f).fillMaxSize(),
            imageLoader = gifImageLoader,
            isClickable = false
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SidebarItem(
    imageUrl: String? = null,
    imageRes: Int? = null,
    modifier: Modifier,
    token: String = "",
    imageLoader: coil.ImageLoader,
    isClickable: Boolean = true,
    onClick: () -> Unit = {}
) {
    if (isClickable) {
        Surface(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
            shape = ClickableSurfaceDefaults.shape(RectangleShape),
            colors = ClickableSurfaceDefaults.colors(containerColor = Color.Transparent, focusedContainerColor = Color.Transparent),
            border = ClickableSurfaceDefaults.border(
                focusedBorder = Border(
                    border = BorderStroke(3.dp, Color.White),
                    shape = RectangleShape
                )
            )
        ) {
            SidebarImageContent(imageUrl, imageRes, token, imageLoader)
        }
    } else {
        Box(modifier = modifier.fillMaxWidth()) {
            SidebarImageContent(imageUrl, imageRes, token, imageLoader)
        }
    }
}

@Composable
fun SidebarImageContent(imageUrl: String?, imageRes: Int?, token: String, imageLoader: coil.ImageLoader) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl ?: imageRes)
            .apply { if (token.isNotEmpty()) addHeader("Authorization", "Bearer $token") }
            .crossfade(true)
            .build(),
        imageLoader = imageLoader,
        contentDescription = null,
        modifier = Modifier.fillMaxSize().background(Color.Black),
        contentScale = ContentScale.Crop
    )
}