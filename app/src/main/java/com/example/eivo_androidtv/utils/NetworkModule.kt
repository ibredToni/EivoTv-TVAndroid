package com.example.eivo_androidtv.utils

import com.example.eivo_androidtv.data.local.SessionManager
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import okhttp3.logging.HttpLoggingInterceptor

// Cambiamos a 'object' para que 'this' tenga sentido y sea un Singleton real
object NetworkModule {
    private var singletonClient: OkHttpClient? = null

    fun createOkHttpClient(sessionManager: SessionManager): OkHttpClient {
        // Usamos el lock del objeto NetworkModule
        return singletonClient ?: synchronized(this) {
            singletonClient ?: OkHttpClient.Builder()
                .connectTimeout(0, TimeUnit.MILLISECONDS)
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .writeTimeout(0, TimeUnit.MILLISECONDS)
                .cookieJar(PersistentCookieJar(sessionManager))
                .addInterceptor { chain ->
                    val originalRequest = chain.request()
                    val token = sessionManager.fetchAuthToken()

                    android.util.Log.d("DEBUG_NET", "Petición a: ${originalRequest.url}")

                    val isAuthPath = originalRequest.url.encodedPath.contains("register-topic") ||
                            originalRequest.url.encodedPath.contains(Constants.ENDPOINT_MERCURE)

                    if (!token.isNullOrBlank() && !isAuthPath) {
                        val newRequest = originalRequest.newBuilder()
                            .addHeader("Authorization", "Bearer $token")
                            .build()
                        chain.proceed(newRequest)
                    } else {
                        chain.proceed(originalRequest)
                    }
                }
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.HEADERS
                })
                .build().also { singletonClient = it }
        }
    }
}

class PersistentCookieJar(private val sessionManager: SessionManager) : CookieJar {
    override fun saveFromResponse(url: HttpUrl, cookies: List<okhttp3.Cookie>) {
        val cookieString = cookies.joinToString("; ") { "${it.name}=${it.value}" }
        if (cookieString.isNotEmpty()) {
            android.util.Log.d("COOKIE_DEBUG", "Guardando cookies de ${url.host}: $cookieString")
            sessionManager.saveCookie(cookieString)
        }
    }

    override fun loadForRequest(url: HttpUrl): List<okhttp3.Cookie> {
        val cookieString = sessionManager.fetchCookie() ?: return listOf()
        val cookies = mutableListOf<okhttp3.Cookie>()

        cookieString.split("; ").forEach {
            val parts = it.split("=")
            if (parts.size == 2) {
                cookies.add(
                    okhttp3.Cookie.Builder()
                        .name(parts[0].trim())
                        .value(parts[1].trim())
                        .domain(url.host)
                        .build()
                )
            }
        }
        return cookies
    }
}