package com.example.eivo_androidtv.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class EventoDto(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val inicio: String, // Formato "2026-02-22 09:55:00"
    val fin: String,
    val img_loc: String? = null,
    val img_vis: String? = null,
    val img_back: String? = null
)