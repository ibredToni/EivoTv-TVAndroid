package com.eivotv.androidtv.data.repository

import com.eivotv.androidtv.data.remote.EivoApiService
import com.eivotv.androidtv.domain.model.AuthResult
import com.eivotv.androidtv.domain.model.QrData
import com.eivotv.androidtv.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRepositoryImpl(
    private val api: EivoApiService
) : AuthRepository {

    override suspend fun getQrCodeData(): QrData {
        val dto = api.fetchRegisterTopic()
        return QrData(uuid = dto.uuid, topic = dto.topic)
    }

    override fun listenForLogin(topic: String): Flow<AuthResult> {
        return api.observeMercure(topic).map { dto ->
            AuthResult(token = dto.token, userEmail = dto.email)
        }
    }
}