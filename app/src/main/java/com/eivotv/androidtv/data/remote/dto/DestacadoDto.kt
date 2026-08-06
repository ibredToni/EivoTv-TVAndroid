package com.eivotv.androidtv.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class DestacadoDto(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val inicio: String,
    val fin: String,
    val img: String,
    var img_loc: String? = null,
    var img_vis: String? = null
)