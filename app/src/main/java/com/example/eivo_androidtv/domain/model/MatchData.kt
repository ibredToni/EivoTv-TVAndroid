package com.example.eivo_androidtv.domain.model

data class MatchData(
    val id: Int,
    val teamA: String,
    val teamB: String,
    val escudoA: Int,
    val escudoB: Int,
    val category: String,
    val date: String,
    val sport: String,
    val idRegion: String,
    val idClub: String? = null,
    val isDirecto: Boolean = false,
    val videoUrl: String = ""
)