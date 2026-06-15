package com.example.eivo_androidtv.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class DeviceTokenDto(
    val fcmToken: String,
    val deviceType: String = "android_tv"
)