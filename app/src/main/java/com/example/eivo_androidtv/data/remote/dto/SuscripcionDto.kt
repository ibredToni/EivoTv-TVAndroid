package com.example.eivo_androidtv.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SuscripcionDto(
    val tipo: String? = null,
    val nombre: String? = null,
    val club: String? = null,
    val categoria: String? = null,
    val escudo: String? = null
)