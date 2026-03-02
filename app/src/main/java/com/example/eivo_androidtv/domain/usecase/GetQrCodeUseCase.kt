package com.example.eivo_androidtv.domain.usecase

import com.example.eivo_androidtv.domain.repository.AuthRepository

class GetQrCodeUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke() = repository.getQrCodeData()
}