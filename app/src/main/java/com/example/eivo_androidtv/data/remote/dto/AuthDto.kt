package com.example.eivo_androidtv.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterTopicResponseDto(
    val success: Boolean,
    val uuid: String,
    val topic: String
)

@Serializable
data class AuthPayloadDto(
    val token: String,
    @SerialName("user")
    val email: String
)