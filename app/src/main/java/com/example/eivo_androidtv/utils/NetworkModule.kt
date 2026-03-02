package com.example.eivo_androidtv.utils

import com.example.eivo_androidtv.data.local.SessionManager
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import okhttp3.logging.HttpLoggingInterceptor

private val persistentCookieJar = object : CookieJar {
    private val cookieStore = mutableMapOf<String, List<okhttp3.Cookie>>()
    override fun saveFromResponse(url: HttpUrl, cookies: List<okhttp3.Cookie>) {
        cookieStore[url.host] = cookies
    }
    override fun loadForRequest(url: HttpUrl): List<okhttp3.Cookie> {
        return cookieStore[url.host] ?: listOf()
    }
}
fun createOkHttpClient(sessionManager: SessionManager): OkHttpClient {
    return OkHttpClient.Builder()
        .connectTimeout(0, TimeUnit.MILLISECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .writeTimeout(0, TimeUnit.MILLISECONDS)
        .cookieJar(persistentCookieJar)
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val token = sessionManager.fetchAuthToken()

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
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()
}