package com.example.eivo_androidtv.ui.screens.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.eivo_androidtv.data.remote.EivoApiService
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.media3.common.MimeTypes.APPLICATION_M3U8
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.example.eivo_androidtv.data.remote.dto.AdDto
import com.example.eivo_androidtv.ui.components.ErrorStateComponent
import com.example.eivo_androidtv.utils.NetworkModule

@Composable
fun PlayerScreen(
    eventId: String,
    apiService: EivoApiService,
    onBack: () -> Unit
) {

    androidx.activity.compose.BackHandler {
        onBack()
    }

    val viewModel: PlayerViewModel = viewModel { PlayerViewModel(apiService, eventId) }
    var isPaused by remember { mutableStateOf(false) }
    var ads by remember { mutableStateOf<List<AdDto>>(emptyList()) }

    LaunchedEffect(Unit) {
        try {
            val adsDeferred = apiService.fetchAdverts()

            ads = adsDeferred.filter { it.position == "pause" }
        } catch (e: Exception) {
            //android.util.Log.e("HOME_ERROR", "Error al cargar datos: ${e.message}")
        }
    }

    when (val state = viewModel.uiState) {
        is PlayerUiState.Loading -> {
            Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        }
        is PlayerUiState.Success -> {
            Box(modifier = Modifier.fillMaxSize()) {
                VideoPlayer(
                    url = state.detalle.url_hd ?: state.detalle.url_sd ?: "",
                    isPaused = isPaused,
                    onManualPauseToggle = { isPaused = it }
                )

                if (isPaused) {
                    PauseScreen(
                        detalle = state.detalle,
                        onResume = { isPaused = false },
                        ads = ads
                    )
                }
            }
        }
        is PlayerUiState.Error -> {
            Box(Modifier.fillMaxSize().background(Color.Black)) {
                ErrorStateComponent(
                    isOffline = state.message.contains("UnknownHostException", ignoreCase = true),
                    onRetry = {
                        viewModel.retryLoadDetails()
                    }
                )
            }
        }
    }
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun VideoPlayer(
    url: String,
    isPaused: Boolean,
    onManualPauseToggle: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { com.example.eivo_androidtv.data.local.SessionManager(context) }
    val myOkHttpClient = remember {
        NetworkModule.createOkHttpClient(sessionManager)
    }

    val exoPlayer = remember {
        val httpDataSourceFactory = androidx.media3.datasource.okhttp.OkHttpDataSource.Factory(myOkHttpClient)
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
            .setDefaultRequestProperties(mapOf(
                "Origin" to "https://apptest.eivotv.es",
                "Referer" to "https://apptest.eivotv.es/",
                "Sec-Fetch-Mode" to "cors"
            ))

        val dataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)

        ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
            .build().apply {
                val mediaItem = MediaItem.Builder()
                    .setUri(url)
                    .setMimeType(APPLICATION_M3U8)
                    .build()

                setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
                addListener(object : Player.Listener {
                    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                        if (reason == Player.PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST ||
                            reason == Player.PLAY_WHEN_READY_CHANGE_REASON_REMOTE) {
                            onManualPauseToggle(!playWhenReady)
                        }
                    }
                })
        }
    }

    LaunchedEffect(isPaused) {
        if (isPaused) exoPlayer.pause() else exoPlayer.play()
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = true
                setBackgroundColor(android.graphics.Color.BLACK)
                isFocusable = true
                isFocusableInTouchMode = true
                requestFocus()
            }
        },
        modifier = Modifier.fillMaxSize(),
        update = { playerView ->
            if (!isPaused) {
                playerView.requestFocus()
            }
        }
    )
}