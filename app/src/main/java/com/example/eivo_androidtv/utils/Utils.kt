package com.example.eivo_androidtv.utils

import kotlin.random.Random

/**
 * Genera un código alfanumérico aleatorio para la vinculación.
 * @param length Longitud del código (por defecto 9 para coincidir con tu diseño)
 */
fun generateRandomCode(length: Int = 9): String {
    val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    return (1..length)
        .map { allowedChars[Random.nextInt(allowedChars.length)] }
        .joinToString("")
}