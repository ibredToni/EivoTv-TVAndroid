package com.example.eivo_androidtv.ui.navigation

sealed class Screen(val route: String) {
    object Linking : Screen("linking")
    object Home : Screen("home")
}