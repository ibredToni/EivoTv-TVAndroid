package com.example.eivo_androidtv.utils

import com.example.eivo_androidtv.BuildConfig

object Constants {
    val BASE_URL = BuildConfig.BASE_URL

    // Prefijo de version
    private const val TV_V1 = "/tv/v1"

    // Endpoints de Autenticacion y Mercure
    const val ENDPOINT_REGISTER = "$TV_V1/register-topic"
    const val ENDPOINT_MERCURE = ".well-known/mercure"

    // Endpoints de Contenido
    const val ENDPOINT_COMUNIDADES = "$TV_V1/comunidades/all"
    const val ENDPOINT_DEPORTES = "$TV_V1/deportes/all"
    const val ENDPOINT_ADVERTS = "$TV_V1/adTv/all"
    const val ENDPOINT_PERFIL = "$TV_V1/user/profile"
    const val ENDPOINT_DESTACADOS = "$TV_V1/destacados/all"
    const val ENDPOINT_REGISTER_FCM = "$TV_V1/devices/tv-register"

    // Endpoints con parámetros dinámicos
    const val PATH_CLUBS = "$TV_V1/clubs" // Ejemplo: /tv/v1/clubs/{idDeporte}
    const val PATH_DIRECTOS = "$TV_V1/directos" // Ejemplo: /tv/v1/directos/{sport}/{club?}
    const val PATH_EVENTOS = "$TV_V1/eventos" // Ejemplo: /tv/v1/evento/{sport}/{date}/{club?}
    const val PATH_EVENTO_DETALLE = "$TV_V1/evento" // Ejemplo: /tv/v1/evento/{idEvento}
}