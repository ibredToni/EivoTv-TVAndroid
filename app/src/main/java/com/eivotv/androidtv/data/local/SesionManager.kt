package com.eivotv.androidtv.data.local

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("eivo_session", Context.MODE_PRIVATE)

    fun saveAuthToken(token: String) {
        prefs.edit().putString("auth_token", token).apply()
    }

    fun fetchAuthToken(): String? {
        return prefs.getString("auth_token", null)
    }

    fun saveCookie(cookie: String) {
        prefs.edit().putString("auth_cookie", cookie).apply()
    }

    fun fetchCookie(): String? {
        return prefs.getString("auth_cookie", null)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}