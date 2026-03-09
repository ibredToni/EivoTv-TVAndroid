package com.example.eivo_androidtv.ui.screens.player

import android.media.browse.MediaBrowser
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
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
import androidx.compose.ui.input.key.onKeyEvent
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

    when (val state = viewModel.uiState) {
        is PlayerUiState.Loading -> {
            Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.Cyan)
            }
        }
        is PlayerUiState.Success -> {
            Box(modifier = Modifier.fillMaxSize()) {
                // 1. El Reproductor siempre debajo
                VideoPlayer(
                    url = state.detalle.url_hd ?: state.detalle.url_sd ?: "",
                    isPaused = isPaused,
                    onPlayerStateChanged = { playing ->
                        isPaused = !playing
                    }
                )

                // 2. Capa de Pausa (Solo se ve si isPaused es true)
                if (isPaused) {
                    PauseScreen(
                        detalle = state.detalle,
                        onResume = { isPaused = false }
                    )
                }
            }
        }
        is PlayerUiState.Error -> {
            Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
                Text(state.message, color = Color.White)
            }
        }
    }
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun VideoPlayer(
    url: String,
    isPaused: Boolean,
    onPlayerStateChanged: (Boolean) -> Unit
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

        val dataSourceFactory = androidx.media3.datasource.DefaultDataSource.Factory(context, httpDataSourceFactory)

        ExoPlayer.Builder(context)
            .setMediaSourceFactory(androidx.media3.exoplayer.source.DefaultMediaSourceFactory(dataSourceFactory))
            .build().apply {
                val mediaItem = MediaItem.Builder()
                    .setUri(url)
                    .setMimeType(androidx.media3.common.MimeTypes.APPLICATION_M3U8)
                    .build()

                setMediaItem(mediaItem)
            prepare()
            playWhenReady = true

            addListener(object : androidx.media3.common.Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    onPlayerStateChanged(isPlaying)
                }
                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    android.util.Log.e("EXO_ERROR", "Error en URL: $url")
                    android.util.Log.e("EXO_ERROR", "Causa: ${error.cause?.message}")
                    android.util.Log.e("EXO_ERROR", "URL intentada: $url")
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
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN) {
                    if (keyEvent.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DPAD_CENTER ||
                        keyEvent.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_ENTER) {
                        if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                        true
                    } else false
                } else false
            }
    )
}