package com.example.eivo_androidtv.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Para pasar de "2026-02-22 09:55:00" a "09:55"
fun formatToTime(apiDate: String?): String {
    if (apiDate.isNullOrEmpty()) return "--:--"
    return try {
        val formatterInput = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val dateTime = LocalDateTime.parse(apiDate, formatterInput)
        dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
    } catch (e: Exception) {
        "--:--"
    }
}