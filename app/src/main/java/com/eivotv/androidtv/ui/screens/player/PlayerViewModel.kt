package com.eivotv.androidtv.ui.screens.player

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eivotv.androidtv.data.remote.EivoApiService
import com.eivotv.androidtv.data.remote.dto.EventoDetalleDto
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.eivotv.androidtv.R

class PlayerViewModel(
    private val apiService: EivoApiService,
    private val eventId: String
) : ViewModel() {

    var uiState by mutableStateOf<PlayerUiState>(PlayerUiState.Loading)
        private set

    init {
        loadEventDetails()
    }

    private fun loadEventDetails() {
        viewModelScope.launch {
            try {
                val detalle = apiService.fetchEventoDetalle(eventId)
                if (detalle.valido) {
                    uiState = PlayerUiState.Success(detalle)
                } else {
                    uiState = PlayerUiState.Error(resId = R.string.no_available_subscription)
                }
            } catch (e: Exception) {
                uiState = PlayerUiState.Error(
                    resId = R.string.error_loading_video,
                    args = listOf(e.message ?: "Unknown"),
                    isNetworkError = e is java.net.UnknownHostException)
            }
        }
    }

    fun retryLoadDetails() {
        loadEventDetails()
    }
}

sealed class PlayerUiState {
    object Loading : PlayerUiState()
    data class Success(val detalle: EventoDetalleDto) : PlayerUiState()
    data class Error(
        val resId: Int,
        val args: List<Any> = emptyList(),
        val isNetworkError: Boolean = false
    ) : PlayerUiState()
}