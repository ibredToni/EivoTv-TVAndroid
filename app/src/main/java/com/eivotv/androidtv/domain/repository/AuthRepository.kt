package com.eivotv.androidtv.domain.repository

import com.eivotv.androidtv.domain.model.AuthResult
import com.eivotv.androidtv.domain.model.QrData
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun getQrCodeData(): QrData
    fun listenForLogin(topic: String): Flow<AuthResult>
}