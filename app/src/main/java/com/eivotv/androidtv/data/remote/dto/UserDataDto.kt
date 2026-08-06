package com.eivotv.androidtv.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDataDto(
    val id: Int,
    val email: String,
    val start_sub_date: String? = null
)