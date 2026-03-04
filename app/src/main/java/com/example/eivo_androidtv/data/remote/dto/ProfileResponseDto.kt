package com.example.eivo_androidtv.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProfileResponseDto(
    val user: UserDataDto,
    val suscripciones: List<SuscripcionDto> = emptyList(),
    val jornadas: Int? = 0,
    val eventos: Int? = 0
)