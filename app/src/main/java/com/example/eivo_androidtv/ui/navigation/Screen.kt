package com.example.eivo_androidtv.ui.navigation

sealed class Screen(val route: String) {
    object Linking : Screen("linking")
    object Home : Screen("home")
    object Matches : Screen("matches/{sport}") {
        fun createRoute(sport: String) = "matches/$sport"
    }
    object ClubSelection : Screen("club_selection")
    object Profile : Screen("profile")
    object Player : Screen("player/{eventId}") {
        fun createRoute(eventId: String) = "player/$eventId"
    }
    object Splash : Screen("splash")
}