package com.example.eivo_androidtv.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ClubDto(
    val id: Int,
    val name: String,
    val image: String,
    val fail: String
)