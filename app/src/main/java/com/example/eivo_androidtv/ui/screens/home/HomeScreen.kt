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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.eivo_androidtv.R
import com.example.eivo_androidtv.Screen
import com.example.eivo_androidtv.data.local.SessionManager
import com.example.eivo_androidtv.data.network.MockApiService
import com.example.eivo_androidtv.data.remote.EivoApiService
import com.example.eivo_androidtv.data.remote.dto.AdDto
import com.example.eivo_androidtv.data.remote.dto.ComunidadDto
import com.example.eivo_androidtv.data.remote.dto.DeporteDto
import com.example.eivo_androidtv.data.remote.dto.DestacadoDto
import com.example.eivo_androidtv.domain.model.BannerData
import com.example.eivo_androidtv.domain.model.AdData
import com.example.eivo_androidtv.ui.components.BackgroundFoci
import com.example.eivo_androidtv.ui.components.ChipGroup
import com.example.eivo_androidtv.ui.components.ProfileIcon
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import android.os.Build

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
    onNavigateToPlayer: (String) -> Unit,
    regions: List<ComunidadDto>,
    sessionManager: SessionManager,
) {
    var destacados by remember { mutableStateOf<List<DestacadoDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var adsHome by remember { mutableStateOf<List<AdDto>>(emptyList()) }
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { destacados.size })
    val topBarFocusRequester = remember { FocusRequester() }
    val sportsChipFocusRequester = remember { FocusRequester() }
    val selectedSportName = sports.find { it.id.toString() == selectedSportId }?.nombre ?: ""
    val token = remember { sessionManager.fetchAuthToken() ?: "" }

    LaunchedEffect(Unit) {
        try {
            destacados = apiService.fetchDestacados()
            val adsDeferred = apiService.fetchAdverts()

            val posicionesValidas = listOf("home_1", "home_2")
            adsHome = adsDeferred.filter { it.position in posicionesValidas }
            android.util.Log.d("ADS_DEBUG", "Anuncios encontrados: ${adsHome.size}")
        } catch (e: Exception) {
            android.util.Log.e("HOME_ERROR", "Error al cargar destacados: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        BackgroundFoci(Color(0xFF269091))

        if (regions.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        } else {

            LaunchedEffect(regions) {
                if (regions.isNotEmpty()) {
                    kotlinx.coroutines.yield()
                    topBarFocusRequester.requestFocus()
                    android.util.Log.d("FOCUS_DEBUG", "Foco solicitado a TopBar")
                }
            }

            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 48.dp, vertical = 24.dp)) {
                Box(modifier = Modifier.focusRequester(topBarFocusRequester)) {
                    HomeTopBar(
                        regions = regions,
                        selectedRegion = selectedRegion,
                        onRegionSelected = onRegionSelected,
                        onNavigateToProfile = onNavigateToProfile,
                        modifier = Modifier.focusRequester(topBarFocusRequester)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    if (destacados.isNotEmpty()) {
                        HeroBanner(
                            banners = destacados,
                            pagerState = pagerState,
                            modifier = Modifier.weight(0.72f),
                            onBannerClick = {
                                val currentEventId = destacados[pagerState.currentPage].id.toString()
                                onNavigateToPlayer(currentEventId)
                            },
                            onMoveDown = { sportsChipFocusRequester.requestFocus() },
                            onMoveUp = { topBarFocusRequester.requestFocus() },
                            token= token
                        )

                        Spacer(modifier = Modifier.width(20.dp))

                        RightSidebar(
                            destacados = destacados,
                            ads = adsHome,
                            currentPagerIndex = pagerState.currentPage,
                            modifier = Modifier.weight(0.28f),
                            token = token,
                            onNextClick = { eventId ->
                                onNavigateToPlayer(eventId)
                            }
                        )
                    } else if (!isLoading) {
                        Box(Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                            Text("No hay contenido destacado", color = Color.Gray)
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
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_eivo_logo),
            contentDescription = null,
            modifier = Modifier.width(90.dp),
            contentScale = ContentScale.Fit
        )

        ChipGroup(
            items = regions.map { it.name },
            selectedItem = selectedRegion?.name ?: "",
            onItemSelected = { nombre ->
                regions.find { it.name == nombre }?.let { onRegionSelected(it) }
            },
            modifier = Modifier.weight(1f).fillMaxWidth(0.5f)
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
                modifier = Modifier.fillMaxSize(),
                shape = RectangleShape,
                border = Border(
                    border = BorderStroke(
                        width = if (isPageFocused) 3.dp else 2.dp,
                        color = if (isPageFocused) Color.White else Color.White.copy(alpha = 0.5f)
                    ),
                    shape = RectangleShape
                ),
                colors = SurfaceDefaults.colors(containerColor = Color.Black)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    LaunchedEffect(data.img) {
                        android.util.Log.d("URL_CHECK", "Intentando cargar: ${data.img.trim()}")
                    }
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(data.img.trim())

                            .crossfade(true)
                            .allowHardware(false)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        error = painterResource(R.drawable.ic_eivo_logo),
                        onError = { state ->
                            android.util.Log.e("EIVO_FAIL", "Error en imagen: ${state.result.throwable.message}")
                        }
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
                    ) {

                        val rawName = data.nombre
                        val displayTitle = if (rawName.length > 23) {
                            rawName.split("-").joinToString("\n") { it.trim() }
                        } else {
                            rawName
                        }

                        Text(
                            text = displayTitle.uppercase(),
                            color = Color.White,
                            fontSize = if (displayTitle.contains("\n")) 28.sp else 38.sp,
                            lineHeight = 34.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 3
                        )
                        Text(
                            text = data.descripcion,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 18.sp
                        )
                        Text(
                            text = data.inicio,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 18.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = onBannerClick,
                            modifier = Modifier
                                .onFocusChanged { state -> isPageFocused = state.isFocused }
                                .onKeyEvent { event ->
                                    if (event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                                        when (event.nativeKeyEvent.keyCode) {
                                            KeyEvent.KEYCODE_DPAD_DOWN -> {
                                                onMoveDown()
                                                true
                                            }
                                            KeyEvent.KEYCODE_DPAD_UP -> {
                                                onMoveUp()
                                                true
                                            }
                                            else -> false
                                        }
                                    } else {
                                        false
                                    }
                                },
                            shape = ButtonDefaults.shape(RectangleShape),
                            colors = ButtonDefaults.colors(
                                containerColor = Color.White.copy(alpha = 0.2f),
                                contentColor = Color.White,
                                focusedContainerColor = Color.White,
                                focusedContentColor = Color.Black
                            ),
                            scale = ButtonDefaults.scale(focusedScale = 1.1f)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_play),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Ver ahora", fontWeight = FontWeight.Bold)
                            }
                        }

                        if (isPageFocused) {
                            Spacer(Modifier.height(20.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                repeat(banners.size) { index ->
                                    val isSelected = pagerState.currentPage == index
                                    Box(
                                        modifier = Modifier
                                            .height(6.dp)
                                            .width(if (isSelected) 24.dp else 6.dp)
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
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    val adHome = remember(ads) { ads.filter { it.position == "home_1" } }
    val adHome2 = remember(ads) { ads.filter { it.position == "home_2" } }
    var currentAd1Index by remember { mutableIntStateOf(0) }
    var currentAd2Index by remember { mutableIntStateOf(0) }
    val nextDestacadoIndex = (currentPagerIndex + 1) % destacados.size
    val nextDestacado = destacados[nextDestacadoIndex]

    LaunchedEffect(adHome, adHome2) {
        while (true) {
            kotlinx.coroutines.delay(5000) // Cambia cada 5 segundos
            if (adHome.isNotEmpty()) {
                currentAd1Index = (currentAd1Index + 1) % adHome.size
            }
            if (adHome2.isNotEmpty()) {
                currentAd2Index = (currentAd2Index + 1) % adHome2.size
            }
        }
    }

    Column(modifier = modifier.fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(16.dp)) {

        if (nextDestacado != null) {
            SidebarItem(
                imageUrl = nextDestacado.img,
                modifier = Modifier.weight(1f),
                token = token,
                imageLoader = gifImageLoader,
                isClickable = true,
                onClick = { onNextClick(nextDestacado.id.toString()) }
            )
        }
        val ad1ToShow = adHome.getOrNull(currentAd1Index)
        SidebarItem(
            imageUrl = ad1ToShow?.url,
            modifier = Modifier.weight(1f),
            token = "",
            imageLoader = gifImageLoader,
            isClickable = false
        )
        val ad2ToShow = adHome2.getOrNull(currentAd2Index)
        SidebarItem(
            imageUrl = ad2ToShow?.url,
            modifier = Modifier.weight(1f),
            token = "",
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
            colors = ClickableSurfaceDefaults.colors(
                containerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent
            ),
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
            .apply {
                if (token.isNotEmpty()) addHeader("Authorization", "Bearer $token")
            }
            .crossfade(true)
            .build(),
        imageLoader = imageLoader,
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
        error = painterResource(R.drawable.ic_eivo_logo)
    )
}