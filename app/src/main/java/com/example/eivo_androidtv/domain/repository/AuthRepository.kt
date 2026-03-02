package com.example.eivo_androidtv.domain.repository

import com.example.eivo_androidtv.domain.model.AuthResult
import com.example.eivo_androidtv.domain.model.QrData
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun getQrCodeData(): QrData
    fun listenForLogin(topic: String): Flow<AuthResult>
}