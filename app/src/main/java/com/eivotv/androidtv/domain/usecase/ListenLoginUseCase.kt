package com.eivotv.androidtv.domain.usecase

import com.eivotv.androidtv.domain.repository.AuthRepository

class ListenLoginUseCase(private val repository: AuthRepository) {
    operator fun invoke(topic: String) = repository.listenForLogin(topic)
}