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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.focus.focusRestorer
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
    val screenCreationTime = remember { System.currentTimeMillis() }

    BackHandler(enabled = !showExitDialog) {
        if (System.currentTimeMillis() - screenCreationTime > 500) {
            showExitDialog = true
        }
    }

    val loadData: suspend (String) -> Unit = { communityId ->
        isLoading = true
        errorOccurred = false
        isOffline = false
        try {
            destacados = apiService.fetchDestacados(communityId)
            val adsDeferred = apiService.fetchAdverts()
            val posicionesValidas = listOf("home_1", "home_2")
            adsHome = adsDeferred.filter { it.position in posicionesValidas }
        } catch (e: Exception) {
            android.util.Log.e("EIVO_HOME", "Error cargando destacados/anuncios", e)
            isOffline = (e is java.net.UnknownHostException || e is java.net.ConnectException || e is java.io.IOException)
            errorOccurred = true
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(selectedRegion) {
        selectedRegion?.let { loadData(it.id.toString()) }
    }

    EivoScreenContainer {
        var focusedSportName by remember { mutableStateOf(selectedSportName) }

        when {
            isInitialLoading -> {
                Box(Modifier.fillMaxSize().focusable().focusRequester(remember { FocusRequester() }), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            }

            errorOccurred -> {
                ErrorStateComponent(
                    isOffline = isOffline,
                    onRetry = { selectedRegion?.let { region -> scope.launch { loadData(region.id.toString()) } } }
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

                    Row(modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (isLoading) {
                            Box(Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Color.White)
                            }
                        } else if (destacados.isNotEmpty()) {
                            HeroBanner(
                                banners = destacados,
                                pagerState = pagerState,
                                modifier = Modifier
                                    .weight(0.75f)
                                    .aspectRatio(16f / 9f),
                                onBannerClick = {
                                    val currentDestacado = destacados[pagerState.currentPage]
                                    onNavigateToPlayer(currentDestacado.id.toString(), currentDestacado.img)
                                },
                                onMoveDown = { sportsChipFocusRequester.requestFocus() },
                                onMoveUp = { topBarFocusRequester.requestFocus() },
                                apiService = apiService
                            )

                            //Spacer(modifier = Modifier.width(12.dp))

                            RightSidebar(
                                destacados = destacados,
                                ads = adsHome,
                                currentPagerIndex = pagerState.currentPage,
                                modifier = Modifier
                                    .weight(0.25f)
                                    .fillMaxHeight(),
                                onNextClick = { eventId ->
                                    val destacado = destacados.find { it.id.toString() == eventId }
                                    onNavigateToPlayer(eventId, destacado?.img)
                                },
                                apiService = apiService
                            )
                        } else {
                            Box(Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                                Text(stringResource(R.string.no_featured_content), color = Color.Gray)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .focusRequester(sportsChipFocusRequester),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ChipGroup(
                            items = sports.map { it.nombre },
                            selectedItem = selectedSportName,
                            onItemSelected = { nombre ->
                                sports.find { it.nombre == nombre }?.let {
                                    onSportSelected(it.id.toString())
                                    onNavigateToMatches(it.id.toString())
                                }
                            },
                            onItemFocused = { nombre ->
                                focusedSportName = nombre
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRestorer()
                                .onKeyEvent { event ->
                                    if (event.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN &&
                                        event.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DPAD_DOWN) {

                                        sports.find { it.nombre == focusedSportName }?.let {
                                            onNavigateToMatches(it.id.toString())
                                        }
                                        true
                                    } else false
                                }
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
            modifier = Modifier.width(120.dp),
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
    apiService: EivoApiService,
) {
    val scope = rememberCoroutineScope()
    var bannerHasFocus by remember { mutableStateOf(false) }

    LaunchedEffect(pagerState.currentPage, banners.size) {
        if (banners.isNotEmpty()) {
            kotlinx.coroutines.delay(5000)
            val nextPage = (pagerState.currentPage + 1) % banners.size

            if (bannerHasFocus) {
                pagerState.animateScrollToPage(nextPage)
            } else {
                pagerState.scrollToPage(nextPage)
            }
        }
    }

    Box(modifier = modifier
        .onFocusChanged { bannerHasFocus = it.hasFocus }
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f),
            beyondViewportPageCount = 2,
            userScrollEnabled = false,
            key = { index -> if (index < banners.size) banners[index].id else index }
        ) { page ->
            val isCurrentPage by remember { derivedStateOf { pagerState.currentPage == page } }
            val data = banners[page]
            val buttonFocusRequester = remember { FocusRequester() }
            var isPageFocused by remember { mutableStateOf(false) }
            var escudoLocal by remember(data.id) { mutableStateOf<String?>(null) }
            var escudoVisitante by remember(data.id) { mutableStateOf<String?>(null) }

            LaunchedEffect(data.id) {
                try {
                    val detalle = apiService.fetchEventoDetalle(data.id.toString())
                    escudoLocal = detalle.img_loc
                    escudoVisitante = detalle.img_vis
                } catch (e: Exception) {
                    // Si falla, se quedan como null y no se muestran
                }
            }

            LaunchedEffect(isCurrentPage) {
                if (isCurrentPage && bannerHasFocus) {
                    kotlinx.coroutines.delay(100)
                    buttonFocusRequester.requestFocus()
                }
            }

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
                            .crossfade(false)
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
                            .fillMaxWidth(0.7f),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .offset(y = 20.dp, x = (-10).dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (escudoLocal != null) {
                                    AsyncImage(
                                        model = escudoLocal,
                                        contentDescription = null,
                                        modifier = Modifier.size(100.dp)
                                    )
                                }
                                if (escudoVisitante != null) {
                                    AsyncImage(
                                        model = escudoVisitante,
                                        contentDescription = null,
                                        modifier = Modifier.size(100.dp)
                                    )
                                }
                            }
                        }

                        val baseTitleSize = 44.sp
                        Column(
                            verticalArrangement = Arrangement.spacedBy((-8).dp)
                        ) {
                            // Título Destacado
                            Text(
                                text = data.nombre.uppercase(),
                                color = Color.White,
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontSize = baseTitleSize,
                                ),
                                maxLines = 1,
                                overflow = Ellipsis
                            )
                            // Descripción
                            Text(
                                text = data.descripcion,
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                                maxLines = 1
                            )
                            // Fecha
                            Text(
                                text = formatToLongDate(data.inicio),
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = onBannerClick,
                            enabled = isCurrentPage,
                            modifier = Modifier
                                .height(26.dp)
                                .wrapContentWidth()
                                .focusRequester(buttonFocusRequester)
                                .onFocusChanged { state -> isPageFocused = state.isFocused }
                                .onKeyEvent { event ->
                                    if (event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                                        when (event.nativeKeyEvent.keyCode) {
                                            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                                                scope.launch {
                                                    val total = banners.size
                                                    if (total > 0) {
                                                        val next = (pagerState.currentPage + 1) % total
                                                        // Si vamos al principio, mejor instantáneo para evitar el efecto "rebobinado"
                                                        if (next == 0) pagerState.scrollToPage(0)
                                                        else pagerState.animateScrollToPage(next)
                                                    }
                                                }
                                                true
                                            }
                                            KeyEvent.KEYCODE_DPAD_LEFT -> {
                                                scope.launch {
                                                    val total = banners.size
                                                    if (total > 0) {
                                                        // Calculamos el anterior de forma segura
                                                        val prev = if (pagerState.currentPage > 0) pagerState.currentPage - 1 else total - 1

                                                        // IMPORTANTE: Si es el salto del inicio al final, usamos scrollToPage
                                                        // Pero para el resto, forzamos el índice exacto
                                                        if (pagerState.currentPage == 0) {
                                                            pagerState.scrollToPage(total - 1)
                                                        } else {
                                                            // Usamos scrollToPage aquí también si animate te sigue fallando,
                                                            // ya que en TV es 100% fiable y no se cancela.
                                                            pagerState.animateScrollToPage(prev)
                                                        }
                                                    }
                                                }
                                                true
                                            }
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

                        Spacer(Modifier.height(10.dp))

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
    onNextClick: (String) -> Unit,
    apiService: EivoApiService,
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
    var escudos by remember(nextDestacado.id) { mutableStateOf<Pair<String?, String?>>(null to null) }

    LaunchedEffect(nextDestacado.id) {
        try {
            val detalle = apiService.fetchEventoDetalle(nextDestacado.id.toString())
            escudos = detalle.img_loc to detalle.img_vis
        } catch (e: Exception) {
            escudos = null to null
        }
    }

    Column(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            SidebarItem(
                imageUrl = nextDestacado.img,
                modifier = Modifier.fillMaxSize(),
                imageLoader = gifImageLoader,
                isClickable = true,
                onClick = { onNextClick(nextDestacado.id.toString()) }
            )

            // Superposicion de escudos
            if (escudos.first != null || escudos.second != null) {
                Row(

                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    escudos.first?.let {
                        AsyncImage(
                            model = it,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp)
                        )
                    }

                    escudos.second?.let {
                        AsyncImage(
                            model = it,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Box(modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
        ) {
            SidebarItem(
                imageUrl = ad1ToShow?.url,
                modifier = Modifier.fillMaxSize(),
                imageLoader = gifImageLoader,
                isClickable = false
            )
        }

        // ESPACIO PEQUEÑO ENTRE ADS
        Spacer(modifier = Modifier.height(4.dp))

        // ELEMENTO 3: Ad 2
        Box(modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
        ) {
            SidebarItem(
                imageUrl = ad2ToShow?.url,
                modifier = Modifier.fillMaxSize(),
                imageLoader = gifImageLoader,
                isClickable = false
            )
        }
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
            modifier = modifier,
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
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentScale = ContentScale.Crop
    )
}