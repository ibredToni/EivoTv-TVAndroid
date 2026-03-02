package com.example.eivo_androidtv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import com.example.eivo_androidtv.ui.screens.club.ClubSelectionScreen
import com.example.eivo_androidtv.ui.screens.home.HomeScreen
import com.example.eivo_androidtv.ui.screens.linking.LoginLinkingScreen
import com.example.eivo_androidtv.ui.theme.Eivo_androidtvTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.eivo_androidtv.ui.screens.profile.ProfileScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eivo_androidtv.data.remote.EivoApiService
import com.example.eivo_androidtv.data.repository.AuthRepositoryImpl
import com.example.eivo_androidtv.domain.usecase.GetQrCodeUseCase
import com.example.eivo_androidtv.domain.usecase.ListenLoginUseCase
import com.example.eivo_androidtv.ui.screens.linking.LinkingViewModel
import com.example.eivo_androidtv.utils.okHttpClient
import com.example.eivo_androidtv.utils.Constants

sealed class Screen(val route: String) {
    object Linking : Screen("linking")
    object Home : Screen("home")
    object Matches : Screen("matches/{sport}") {
        fun createRoute(sport: String) = "matches/$sport"
    }
    object ClubSelection : Screen("club_selection")
    object Profile : Screen("profile")
    object Pause : Screen("pause")
}

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Eivo_androidtvTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RectangleShape,
                    colors = SurfaceDefaults.colors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    )
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    var globalRegion by remember { mutableStateOf("Baleares") }

    NavHost(
        navController = navController,
        startDestination = Screen.Linking.route
    ) {
        composable(Screen.Linking.route) {
            val apiService = remember { EivoApiService(okHttpClient, Constants.BASE_URL) }
            val repository = remember { AuthRepositoryImpl(apiService) }
            val getQrUseCase = remember { GetQrCodeUseCase(repository) }
            val listenUseCase = remember { ListenLoginUseCase(repository) }

            val linkingViewModel: LinkingViewModel = viewModel {
                LinkingViewModel(getQrUseCase, listenUseCase)
            }

            LoginLinkingScreen(
                viewModel = linkingViewModel,
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Linking.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToMatches = { sport ->
                    navController.navigate("matches/$sport")
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToPlayer = {
                    navController.navigate(Screen.Pause.route)
                }
            )
        }

        composable(Screen.Matches.route) { backStackEntry ->
            val sport = backStackEntry.arguments?.getString("sport") ?: ""

            val selectedClubId by backStackEntry.savedStateHandle
                .getStateFlow<String?>("selectedClub", null)
                .collectAsStateWithLifecycle()

            com.example.eivo_androidtv.ui.screens.matches.MatchesScreen(
                initialSport = sport,
                selectedRegion = globalRegion, // Pasamos el estado global
                onRegionSelected = { globalRegion = it }, // Permitimos cambiarlo
                filterClubId = selectedClubId, // Pasamos el ID del club seleccionado
                onNavigateToClubSelection = {
                    navController.navigate(Screen.ClubSelection.route)
                },
                onClearClubFilter = {
                    backStackEntry.savedStateHandle["selectedClub"] = null
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }

        composable(Screen.ClubSelection.route) {
            ClubSelectionScreen(
                selectedRegion = globalRegion,

                onClubSelected = { clubId ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("selectedClub", clubId)
                    navController.popBackStack()
                },
                onBackWithoutSelection = {
                    navController.popBackStack()
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Linking.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Pause.route) {
            com.example.eivo_androidtv.ui.screens.player.PauseScreen(
                onResume = { navController.popBackStack() }
            )
        }
    }
}