package com.eivotv.androidtv.ui.screens.linking

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eivotv.androidtv.domain.usecase.GetQrCodeUseCase
import com.eivotv.androidtv.domain.usecase.ListenLoginUseCase
import kotlinx.coroutines.launch
import com.eivotv.androidtv.data.local.SessionManager

class LinkingViewModel(
    private val getQrCodeUseCase: GetQrCodeUseCase,
    private val listenLoginUseCase: ListenLoginUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    var uiState by mutableStateOf(LinkingUiState())
        private set

    fun loadQrAndListen() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            try {
                val qrData = getQrCodeUseCase()
                uiState = uiState.copy(uuid = qrData.uuid, isLoading = false)

                launch {
                    //android.util.Log.e("MERCURE_TEST", "Iniciando escucha de: ${qrData.topic}")
                    listenLoginUseCase(qrData.topic)
                        .collect { authResult ->
                            //android.util.Log.e("MERCURE_TEST", "¡Token recibido en VM!")
                            uiState = uiState.copy(isLoggedIn = true, token = authResult.token)
                            sessionManager.saveAuthToken(authResult.token)
                        }
                }
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = e.message)
            }
        }
    }
}

data class LinkingUiState(
    val uuid: String? = null,
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val token: String? = null,
    val error: String? = null
)