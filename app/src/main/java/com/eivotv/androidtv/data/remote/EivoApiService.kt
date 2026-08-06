package com.eivotv.androidtv.data.remote

import com.eivotv.androidtv.data.remote.dto.AdDto
import com.eivotv.androidtv.data.remote.dto.AuthPayloadDto
import com.eivotv.androidtv.data.remote.dto.ClubDto
import com.eivotv.androidtv.data.remote.dto.RegisterTopicResponseDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import com.eivotv.androidtv.data.remote.dto.ComunidadDto
import com.eivotv.androidtv.data.remote.dto.DeporteDto
import com.eivotv.androidtv.data.remote.dto.DestacadoDto
import com.eivotv.androidtv.data.remote.dto.EventoDetalleDto
import com.eivotv.androidtv.data.remote.dto.EventoDto
import com.eivotv.androidtv.data.remote.dto.ProfileResponseDto
import com.eivotv.androidtv.utils.Constants
import com.eivotv.androidtv.data.remote.dto.DeviceTokenDto
import kotlinx.serialization.encodeToString
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType


class EivoApiService(private val client: OkHttpClient, private val baseUrl: String) {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun fetchRegisterTopic(): RegisterTopicResponseDto = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$baseUrl${Constants.ENDPOINT_REGISTER}")
            .header("Accept", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Error: ${response.code}")
            val bodyString = response.body?.string() ?: ""
            json.decodeFromString<RegisterTopicResponseDto>(bodyString)
        }
    }

    fun observeMercure(topic: String): Flow<AuthPayloadDto> = callbackFlow {
        val sseUrl = baseUrl.toHttpUrl().newBuilder()
            .addPathSegments(Constants.ENDPOINT_MERCURE)
            .addQueryParameter("topic", topic)
            .build()

        val requestBuilder = Request.Builder()
            .url(sseUrl)
            .header("Accept", "text/event-stream")
            .header("Cache-Control", "no-cache")

        val listener = object : EventSourceListener() {
            override fun onOpen(eventSource: EventSource, response: Response) {
                //android.util.Log.e("MERCURE_TEST", "CONEXIÓN ABIERTA")
            }

            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                //android.util.Log.e("MERCURE_TEST", "Tipo: $type")
                //android.util.Log.e("MERCURE_TEST", "Data: $data")

                try {
                    val payload = json.decodeFromString<AuthPayloadDto>(data)
                    trySend(payload)
                } catch (e: Exception) {
                    //android.util.Log.e("MERCURE_TEST", "Error al parsear JSON: ${e.message}")
                }
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                //android.util.Log.e("MERCURE_TEST", "Mensaje: ${t?.message}")
                //android.util.Log.e("MERCURE_TEST", "Código HTTP: ${response?.code}")
            }

            override fun onClosed(eventSource: EventSource) {
                //android.util.Log.e("MERCURE_TEST", "CONEXIÓN CERRADA POR EL SERVIDOR")
            }
        }

        val eventSource = EventSources.createFactory(client)
            .newEventSource(requestBuilder.build(), listener)

        awaitClose { eventSource.cancel() }
    }

    suspend fun fetchComunidades(): List<ComunidadDto> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$baseUrl${Constants.ENDPOINT_COMUNIDADES}")
            .header("Accept", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Error en Comunidades: ${response.code}")

            val bodyString = response.body?.string() ?: "[]"
            json.decodeFromString<List<ComunidadDto>>(bodyString)
        }
    }

    suspend fun fetchDeportes(communityId: String): List<DeporteDto> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$baseUrl${Constants.ENDPOINT_DEPORTES}")
            .header("Accept", "application/json")
            .header("community", communityId)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Error en Deportes: ${response.code}")

            val bodyString = response.body?.string() ?: "[]"
            json.decodeFromString<List<DeporteDto>>(bodyString)
        }
    }


    suspend fun fetchClubs(communityId: String, deporteId: String): List<ClubDto> = withContext(Dispatchers.IO) {
        val url = "$baseUrl${Constants.PATH_CLUBS}/$deporteId"

        val request = Request.Builder()
            .url(url)
            .header("Accept", "application/json")
            .header("community", communityId)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Error en Clubs: ${response.code}")
            }

            val bodyString = response.body?.string() ?: "[]"
            json.decodeFromString<List<ClubDto>>(bodyString)
        }
    }

    suspend fun fetchEventos(
        communityId: String,
        deporteId: String,
        fecha: String,
        clubId: String? = null
    ): List<EventoDto> = withContext(Dispatchers.IO) {

        val endpoint = if (clubId.isNullOrEmpty()) {
            "${Constants.PATH_EVENTOS}/$deporteId/$fecha"
        } else {
            "${Constants.PATH_EVENTOS}/$deporteId/$fecha/$clubId"
        }

        val request = Request.Builder()
            .url("$baseUrl$endpoint")
            .header("Accept", "application/json")
            .header("community", communityId) // Header obligatorio
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Error en Eventos: ${response.code}")
            }

            val bodyString = response.body?.string() ?: "[]"
            json.decodeFromString<List<EventoDto>>(bodyString)
        }
    }

    suspend fun fetchDirectos(
        communityId: String,
        deporteId: String,
        clubId: String? = null
    ): List<EventoDto> = withContext(Dispatchers.IO) {

        val endpoint = if (clubId.isNullOrEmpty()) {
            "${Constants.PATH_DIRECTOS}/$deporteId"
        } else {
            "${Constants.PATH_DIRECTOS}/$deporteId/$clubId"
        }

        val request = Request.Builder()
            .url("$baseUrl$endpoint")
            .header("Accept", "application/json")
            .header("community", communityId)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Error en Directos: ${response.code}")
            }

            val bodyString = response.body?.string() ?: "[]"
            json.decodeFromString<List<EventoDto>>(bodyString)
        }
    }

    suspend fun fetchDestacados(communityId: String): List<DestacadoDto> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$baseUrl${Constants.ENDPOINT_DESTACADOS}")
            .header("Accept", "application/json")
            .header("community", communityId)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Error en Destacados: ${response.code}")

            val bodyString = response.body?.string() ?: "[]"
            json.decodeFromString<List<DestacadoDto>>(bodyString)
        }
    }

    suspend fun fetchAdverts(): List<AdDto> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$baseUrl${Constants.ENDPOINT_ADVERTS}")
            .header("Accept", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Error en Anuncios: ${response.code}")

            val bodyString = response.body?.string() ?: "[]"
            json.decodeFromString<List<AdDto>>(bodyString)
        }
    }

    suspend fun fetchUserProfile(): ProfileResponseDto = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$baseUrl${Constants.ENDPOINT_PERFIL}")
            .header("Accept", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Error en Perfil: ${response.code}")

            val bodyString = response.body?.string() ?: throw IOException("Cuerpo vacío")

            val list = json.decodeFromString<List<ProfileResponseDto>>(bodyString)
            list.first()
        }
    }

    suspend fun fetchEventoDetalle(idEvento: String): EventoDetalleDto = withContext(Dispatchers.IO) {
        val url = "$baseUrl${Constants.PATH_EVENTO_DETALLE}/$idEvento"

        val request = Request.Builder()
            .url(url)
            .header("Accept", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Error en Detalle Evento: ${response.code}")

            val bodyString = response.body?.string() ?: throw IOException("Cuerpo vacío")

            val list = json.decodeFromString<List<EventoDetalleDto>>(bodyString)
            list.first()
        }
    }

    suspend fun registerDeviceToken(deviceTokenDto: DeviceTokenDto): Unit = withContext(Dispatchers.IO) {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val jsonBody = json.encodeToString(deviceTokenDto)
        val requestBody = jsonBody.toRequestBody(mediaType)

        val request = Request.Builder()
            .url("$baseUrl${Constants.ENDPOINT_REGISTER_FCM}")
            .post(requestBody)
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Error registering FCM token: ${response.code}")
            }
        }
    }
}