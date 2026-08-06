package com.eivotv.androidtv.domain.usecase

import com.eivotv.androidtv.domain.repository.AuthRepository

class GetQrCodeUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke() = repository.getQrCodeData()
}