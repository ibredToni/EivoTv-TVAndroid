package com.eivotv.androidtv.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class EventoDetalleDto(
    val id: Int,
    val valido: Boolean,
    val nombre: String? = null,
    val descripcion: String? = null,
    val inicio: String? = null,
    val fin: String? = null,
    val img_loc: String? = null,
    val img_vis: String? = null,
    val tipo_stream: String? = null,
    val url_hd: String? = null,
    val url_sd: String? = null,
    val id_visualizacion: Int? = null
)