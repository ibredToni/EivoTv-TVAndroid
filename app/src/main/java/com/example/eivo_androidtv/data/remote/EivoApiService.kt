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

class EivoApiService(private val client: OkHttpClient, private val baseUrl: String) {

    private val json = Json { ignoreUnknownKeys = true }
    private var lastCookie: String? = null

    suspend fun fetchRegisterTopic(): RegisterTopicResponseDto = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$baseUrl/tv/v1/register-topic")
            .header("Accept", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Error: ${response.code}")

            val cookieHeader = response.header("Set-Cookie")
            if (cookieHeader != null) {
                lastCookie = cookieHeader.split(";")[0] // Guardamos solo el valor
            }

            val bodyString = response.body?.string() ?: ""
            json.decodeFromString<RegisterTopicResponseDto>(bodyString)
        }
    }

    fun observeMercure(topic: String): Flow<AuthPayloadDto> = callbackFlow {
        val sseUrl = baseUrl.toHttpUrl().newBuilder()
            .addPathSegments(".well-known/mercure")
            .addQueryParameter("topic", topic)
            .build()

        val requestBuilder = Request.Builder()
            .url(sseUrl)
            .header("Accept", "text/event-stream")
            .header("Cache-Control", "no-cache")

        lastCookie?.let {
            requestBuilder.header("Cookie", it)
            android.util.Log.d("MERCURE_DEBUG", "Enviando Cookie manual: $it")
        }

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
}