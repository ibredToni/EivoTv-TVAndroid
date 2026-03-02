package com.example.eivo_androidtv.domain.usecase

import com.example.eivo_androidtv.domain.repository.AuthRepository

class ListenLoginUseCase(private val repository: AuthRepository) {
    operator fun invoke(topic: String) = repository.listenForLogin(topic)
}