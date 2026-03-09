package com.example.eivo_androidtv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.eivo_androidtv.data.local.SessionManager
import com.example.eivo_androidtv.data.remote.EivoApiService
import com.example.eivo_androidtv.data.remote.dto.ComunidadDto
import com.example.eivo_androidtv.data.remote.dto.DeporteDto
import com.example.eivo_androidtv.data.repository.AuthRepositoryImpl
import com.example.eivo_androidtv.domain.usecase.GetQrCodeUseCase
import com.example.eivo_androidtv.domain.usecase.ListenLoginUseCase
import com.example.eivo_androidtv.ui.screens.linking.LinkingViewModel
import com.example.eivo_androidtv.ui.screens.matches.MatchesScreen
import com.example.eivo_androidtv.ui.screens.player.PlayerScreen
import com.example.eivo_androidtv.utils.Constants
import com.example.eivo_androidtv.utils.NetworkModule

sealed class Screen(val route: String) {
    object Linking : Screen("linking")
    object Home : Screen("home")
    object Matches : Screen("matches/{sport}") {
        fun createRoute(sport: String) = "matches/$sport"
    }
    object ClubSelection : Screen("club_selection")
    object Profile : Screen("profile")
    object Pause : Screen("pause")

    object Player : Screen("player/{eventId}") {
        fun createRoute(eventId: String) = "player/$eventId"
    }
}

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionManager = SessionManager(this)
        val sharedClient = NetworkModule.createOkHttpClient(sessionManager)

        val imageLoader = coil.ImageLoader.Builder(this)
            .okHttpClient(sharedClient)
            .build()

        coil.Coil.setImageLoader(imageLoader)

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
    val context = androidx.compose.ui.platform.LocalContext.current
    val sessionManager = remember { com.example.eivo_androidtv.data.local.SessionManager(context) }
    val apiService = remember {
        EivoApiService(
            client = NetworkModule.createOkHttpClient(sessionManager),
            baseUrl = Constants.BASE_URL
        )
    }

    // --- ESTADO DE NAVEGACIÓN ---
    val initialToken = remember { sessionManager.fetchAuthToken() }
    var startRoute by remember { mutableStateOf(if (initialToken.isNullOrEmpty()) Screen.Linking.route else Screen.Home.route) }

    // --- ESTADO GLOBAL ---
    var globalRegions by remember { mutableStateOf<List<ComunidadDto>>(emptyList()) }
    var globalRegion by remember { mutableStateOf<ComunidadDto?>(null) }
    var globalSports by remember { mutableStateOf<List<DeporteDto>>(emptyList()) }
    var globalSportId by remember { mutableStateOf("") }

    LaunchedEffect(startRoute, initialToken) {
        val currentToken = sessionManager.fetchAuthToken()
        if (!currentToken.isNullOrEmpty() && globalRegions.isEmpty()) {
            try {
                android.util.Log.d("NAV_DEBUG", "Cargando comunidades...")
                val fetchedRegions = apiService.fetchComunidades()
                globalRegions = fetchedRegions
                if (globalRegion == null && fetchedRegions.isNotEmpty()) {
                    globalRegion = fetchedRegions.first()
                }
            } catch (e: Exception) {
                android.util.Log.e("NAV_ERROR", "Error: ${e.message}")
                if (e.message?.contains("401") == true) {
                    sessionManager.clearSession()
                    navController.navigate(Screen.Linking.route) { popUpTo(0) }
                }
            }
        }
    }

    LaunchedEffect(globalRegion) {
        globalRegion?.let { region ->
            try {
                val fetched = apiService.fetchDeportes(region.id.toString())
                globalSports = fetched
                if (globalSportId.isEmpty() && fetched.isNotEmpty()) {
                    globalSportId = fetched.first().id.toString()
                }
            } catch (e: Exception) {
                android.util.Log.e("NAV_ERROR", "Error cargando deportes: ${e.message}")
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startRoute
    ) {
        composable(Screen.Linking.route) {
            val repository = remember { AuthRepositoryImpl(apiService) }
            val getQrUseCase = remember { GetQrCodeUseCase(repository) }
            val listenUseCase = remember { ListenLoginUseCase(repository) }

            val linkingViewModel: LinkingViewModel = viewModel {
                LinkingViewModel(getQrUseCase, listenUseCase, sessionManager)
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
                apiService = apiService,
                regions = globalRegions,
                selectedRegion = globalRegion,
                onRegionSelected = { globalRegion = it },
                selectedSportId = globalSportId,
                onSportSelected = { globalSportId = it },
                sports = globalSports,
                sessionManager = sessionManager,
                onNavigateToMatches = { sportId ->
                    globalSportId = sportId //
                    navController.navigate(Screen.Matches.createRoute(sportId))
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToPlayer = { eventId ->
                    navController.navigate(Screen.Player.createRoute(eventId))
                },
            )
        }

        composable(Screen.Matches.route) { backStackEntry ->
            val sportIdFromRoute = backStackEntry.arguments?.getString("sport") ?: globalSportId

            val selectedClubId by backStackEntry.savedStateHandle
                .getStateFlow<String?>("selectedClub", null)
                .collectAsStateWithLifecycle()

            MatchesScreen(
                initialSport = sportIdFromRoute,
                selectedRegion = globalRegion,
                onRegionSelected = { globalRegion = it },
                filterClubId = selectedClubId,
                apiService = apiService,
                sports = globalSports,
                onSportSelected = { globalSportId = it },
                onNavigateToClubSelection = {
                    navController.navigate(Screen.ClubSelection.route)
                },
                onClearClubFilter = {
                    backStackEntry.savedStateHandle["selectedClub"] = null
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToPlayer = { eventId ->
                    navController.navigate(Screen.Player.createRoute(eventId))
                }
            )
        }

        composable(Screen.ClubSelection.route) {
            ClubSelectionScreen(
                selectedRegion = globalRegion,
                apiService = apiService,
                selectedSportId = globalSportId,
                onClubSelected = { clubId ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("selectedClub", clubId)
                    navController.popBackStack()
                },
                sports = globalSports,
                onSportSelected = { globalSportId = it },
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
                sessionManager = sessionManager,
                onNavigateToLogin = {
                    navController.navigate(Screen.Linking.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Player.route) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            PlayerScreen(
                eventId = eventId,
                apiService = apiService,
                onBack = { navController.popBackStack() }
            )
        }
    }
}