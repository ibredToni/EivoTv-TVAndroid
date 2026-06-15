package com.example.eivo_androidtv.data.remote.fcm

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.util.Log
import com.example.eivo_androidtv.MainActivity
import com.example.eivo_androidtv.data.local.SessionManager
import com.example.eivo_androidtv.data.remote.EivoApiService
import com.example.eivo_androidtv.data.remote.dto.DeviceTokenDto
import com.example.eivo_androidtv.utils.Constants
import com.example.eivo_androidtv.utils.NetworkModule
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import android.app.ActivityOptions

class FirebaseMessagingService : com.google.firebase.messaging.FirebaseMessagingService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "EIVO_FCM"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val sessionManager = SessionManager(applicationContext)
        val authToken = sessionManager.fetchAuthToken()
        registerDeviceToken(token, authToken)
    }

    private fun registerDeviceToken(fcmToken: String, authToken: String?) {
        if (authToken.isNullOrEmpty()) return
        val client = NetworkModule.createOkHttpClient(SessionManager(applicationContext))
        val apiService = EivoApiService(client = client, baseUrl = Constants.BASE_URL)
        serviceScope.launch {
            try {
                apiService.registerDeviceToken(DeviceTokenDto(fcmToken = fcmToken))
            } catch (e: Exception) {
                Log.e(TAG, "Failed to register token: ${e.message}")
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "¡PUSH DETECTADA POR EL SERVICIO! Entrando en acción...")

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "eivotv:wakelock"
        )
        wakeLock.acquire(3000)

        if (remoteMessage.data.isNotEmpty()) {
            val eventId = remoteMessage.data["eventId"]
            if (!eventId.isNullOrEmpty()) {
                Log.d(TAG, "Procesando evento en segundo plano para ID: $eventId")
                launchPlayer(eventId)
            }
        }
    }

    private fun launchPlayer(eventId: String) {
        val deepLinkUri = Uri.parse("eivotv://player/$eventId")
        val context = applicationContext

        val intent = Intent(Intent.ACTION_VIEW, deepLinkUri, context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                val options = ActivityOptions.makeBasic()

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    options.setPendingIntentBackgroundActivityStartMode(ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED)
                }

                context.startActivity(intent, options.toBundle())
            } else {
                context.startActivity(intent)
            }

            Log.d(TAG, "startActivity ejecutado hacia el frente con opciones de Background.")
        } catch (e: Exception) {
            Log.e(TAG, "Error al levantar la pantalla: ${e.message}")
        }
    }
}