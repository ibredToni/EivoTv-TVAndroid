package com.eivotv.androidtv.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AdDto(
    val position: String,
    val url: String
)