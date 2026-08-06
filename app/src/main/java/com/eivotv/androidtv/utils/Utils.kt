package com.eivotv.androidtv.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

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
// Para pasar de "2026-02-22 09:55:00" a "Domingo 22 de febrero '26 - 09:55"
fun formatToLongDate(apiDate: String?): String {
    if (apiDate.isNullOrEmpty()) return ""

    return try {
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val dateTime = LocalDateTime.parse(apiDate, inputFormatter)

        val SpanishLocale = Locale("es", "ES")
        val outputFormatter = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM ''yy ' - ' HH:mm", SpanishLocale)

        val result = dateTime.format(outputFormatter)
        result.replaceFirstChar { if (it.isLowerCase()) it.titlecase(SpanishLocale) else it.toString() }

    } catch (e: Exception) {
        apiDate
    }
}