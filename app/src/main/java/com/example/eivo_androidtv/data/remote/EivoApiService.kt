package com.example.eivo_androidtv.data.remote

import com.example.eivo_androidtv.data.remote.dto.AuthPayloadDto
import com.example.eivo_androidtv.data.remote.dto.RegisterTopicResponseDto
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
import com.example.eivo_androidtv.data.remote.dto.ComunidadDto
import com.example.eivo_androidtv.data.remote.dto.DeporteDto
import com.example.eivo_androidtv.utils.Constants

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
                android.util.Log.e("MERCURE_TEST", "CONEXIÓN ABIERTA")
            }

            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                android.util.Log.e("MERCURE_TEST", "¡EVENTO RECIBIDO!")
                android.util.Log.e("MERCURE_TEST", "Tipo: $type")
                android.util.Log.e("MERCURE_TEST", "Data: $data")

                try {
                    val payload = json.decodeFromString<AuthPayloadDto>(data)
                    trySend(payload)
                } catch (e: Exception) {
                    android.util.Log.e("MERCURE_TEST", "Error al parsear JSON: ${e.message}")
                }
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                android.util.Log.e("MERCURE_TEST", "FALLO EN SSE")
                android.util.Log.e("MERCURE_TEST", "Mensaje: ${t?.message}")
                android.util.Log.e("MERCURE_TEST", "Código HTTP: ${response?.code}")
            }

            override fun onClosed(eventSource: EventSource) {
                android.util.Log.e("MERCURE_TEST", "CONEXIÓN CERRADA POR EL SERVIDOR")
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
}