package com.example.eivo_androidtv.ui.screens.player

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eivo_androidtv.data.remote.EivoApiService
import com.example.eivo_androidtv.data.remote.dto.EventoDetalleDto
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

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
                    uiState = PlayerUiState.Error("No tienes suscripción activa para este evento.")
                }
            } catch (e: Exception) {
                uiState = PlayerUiState.Error("Error al cargar el video: ${e.message}")
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
    data class Error(val message: String) : PlayerUiState()
}