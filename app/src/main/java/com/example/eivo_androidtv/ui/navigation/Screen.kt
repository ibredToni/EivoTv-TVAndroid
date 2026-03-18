package com.example.eivo_androidtv.ui.navigation

import android.net.Uri

sealed class Screen(val route: String) {
    object Linking : Screen("linking")
    object Home : Screen("home")
    object Matches : Screen("matches/{sport}") {
        fun createRoute(sport: String) = "matches/$sport"
    }
    object ClubSelection : Screen("club_selection")
    object Profile : Screen("profile")
    object Player : Screen("player/{eventId}?backgroundImage={backgroundImage}") {
        fun createRoute(eventId: String, backgroundImage: String? = null): String {
            val route = "player/$eventId"
            return if (backgroundImage.isNullOrBlank()) {
                route
            } else {
                "$route?backgroundImage=${Uri.encode(backgroundImage)}"
            }
        }
    }
    object Splash : Screen("splash")
}
