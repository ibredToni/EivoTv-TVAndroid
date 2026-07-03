package com.example.eivo_androidtv

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
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
import com.example.eivo_androidtv.ui.navigation.Screen
import com.example.eivo_androidtv.ui.screens.linking.LinkingViewModel
import com.example.eivo_androidtv.ui.screens.matches.MatchesScreen
import com.example.eivo_androidtv.ui.screens.player.PlayerScreen
import com.example.eivo_androidtv.ui.screens.splash.SplashScreen
import com.example.eivo_androidtv.utils.Constants
import com.example.eivo_androidtv.utils.NetworkModule
import java.time.LocalDate
import androidx.tv.material3.MaterialTheme
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.view.WindowManager
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import android.provider.Settings
import android.net.Uri
import com.example.eivo_androidtv.ui.components.ConfirmationDialog
import kotlinx.coroutines.flow.StateFlow

class MainActivity : ComponentActivity() {
    private val intentFlow = MutableStateFlow<Intent?>(null)
    private val overlayPermissionNeeded = MutableStateFlow(false)

    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                android.util.Log.e("EIVO_FCM", "TOKEN ACTUAL RECUPERADO: ${task.result}")
            } else {
                android.util.Log.e("EIVO_FCM", "Error al recuperar el token de Firebase", task.exception)
            }
        }

        intentFlow.value = intent

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        overlayPermissionNeeded.value = !hasOverlayPermission()

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
                    AppNavigation(
                        intentFlow = intentFlow,
                        overlayPermissionNeeded = overlayPermissionNeeded,
                        onRequestOverlayPermission = { requestOverlayPermission() }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        overlayPermissionNeeded.value = !hasOverlayPermission()
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intentFlow.value = intent

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
    }

    private fun hasOverlayPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this)

    private fun requestOverlayPermission() {
        try {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                android.util.Log.w("EIVO_PERMISSIONS", "Este dispositivo no soporta ACTION_MANAGE_OVERLAY_PERMISSION")
            }
        } catch (e: Exception) {
            android.util.Log.w("EIVO_PERMISSIONS", "No se pudo abrir el ajuste de superposición", e)
        }
    }
}

@Composable
fun AppNavigation(
    intentFlow: MutableStateFlow<Intent?>,
    overlayPermissionNeeded: StateFlow<Boolean>,
    onRequestOverlayPermission: () -> Unit
) {
    val navController = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current
    val initialIntent = intentFlow.value
    val isDeepLinkLaunch = initialIntent?.data?.let { uri ->
        uri.scheme == "eivotv" && uri.host == "player"
    } ?: false

    var showSplash by remember { mutableStateOf(!isDeepLinkLaunch) }
    val needsOverlayPermission by overlayPermissionNeeded.collectAsState()
    var showOverlayPermissionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(needsOverlayPermission, showSplash) {
        showOverlayPermissionDialog = needsOverlayPermission && !showSplash
    }
    val sessionManager = remember { SessionManager(context) }
    var currentToken by remember { mutableStateOf(sessionManager.fetchAuthToken()) }

    LaunchedEffect(intentFlow) {
        intentFlow.collect { intent ->
            val uri = intent?.data
            if (uri != null && uri.scheme == "eivotv" && uri.host == "player") {
                if (!showSplash) {
                    navController.handleDeepLink(intent)
                    intentFlow.value = null // Limpieza segura
                }
            }
        }
    }

    if (showSplash) {
        SplashScreen(
            eivoColor = MaterialTheme.colorScheme.primary,
            onSplashFinished = { showSplash = false }
        )
    } else {
        val sessionManager = remember { SessionManager(context) }
        LaunchedEffect(Unit) {
            val sharedClient = NetworkModule.createOkHttpClient(sessionManager)
            val imageLoader = coil.ImageLoader.Builder(context)
                .okHttpClient(sharedClient)
                .build()
            coil.Coil.setImageLoader(imageLoader)
        }
        val apiService = remember {
            EivoApiService(
                client = NetworkModule.createOkHttpClient(sessionManager),
                baseUrl = Constants.BASE_URL
            )
        }

        // --- ESTADO DE NAVEGACIÓN ---
        var currentToken by remember { mutableStateOf(sessionManager.fetchAuthToken()) }
        var isInitialDataLoading by remember { mutableStateOf(false) }

        // --- ESTADO GLOBAL ---
        var globalRegions by remember { mutableStateOf<List<ComunidadDto>>(emptyList()) }
        var globalRegion by remember { mutableStateOf<ComunidadDto?>(null) }
        var globalSports by remember { mutableStateOf<List<DeporteDto>>(emptyList()) }
        var globalSportId by remember { mutableStateOf("") }

        LaunchedEffect(currentToken) {
            if (!currentToken.isNullOrEmpty() && globalRegions.isEmpty()) {
                isInitialDataLoading = true

                try {
                    val fetchedRegions = apiService.fetchComunidades()
                    globalRegions = fetchedRegions
                    if (globalRegion == null && fetchedRegions.isNotEmpty()) {
                        globalRegion = fetchedRegions.first()
                    }
                } catch (e: Exception) {
                    //android.util.Log.e("NAV_ERROR", "Error: ${e.message}")
                    if (e.message?.contains("401") == true) {
                        sessionManager.clearSession()
                        navController.navigate(Screen.Linking.route) { popUpTo(0) }
                    }
                } finally {
                    isInitialDataLoading = false
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
                    //android.util.Log.e("NAV_ERROR", "Error cargando deportes: ${e.message}")
                }
            }
        }
        NavHost(
            navController = navController,
            startDestination = if (currentToken.isNullOrEmpty()) Screen.Linking.route else Screen.Home.route
        ) {
            composable(Screen.Linking.route) {
                val repository = remember { AuthRepositoryImpl(apiService) }
                val getQrUseCase = remember { GetQrCodeUseCase(repository) }
                val listenUseCase = remember { ListenLoginUseCase(repository) }

                val linkingViewModel: LinkingViewModel = viewModel {
                    LinkingViewModel(getQrUseCase, listenUseCase, sessionManager)
                }

                val coroutineScope = rememberCoroutineScope()

                LoginLinkingScreen(
                    viewModel = linkingViewModel,
                    onNavigateToHome = {
                        currentToken = sessionManager.fetchAuthToken()

                        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val fcmToken = task.result
                                coroutineScope.launch(Dispatchers.IO) {
                                    try {
                                        val payload = com.example.eivo_androidtv.data.remote.dto.DeviceTokenDto(fcmToken = fcmToken)
                                        apiService.registerDeviceToken(payload)
                                        android.util.Log.i("EIVO_FCM", "FCM token asociado al usuario con éxito tras Login.")
                                    } catch (e: Exception) {
                                        android.util.Log.e("EIVO_FCM", "Error al asociar el token: ${e.message}")
                                    }
                                }
                            }
                        }

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
                    isInitialLoading = isInitialDataLoading,
                    onNavigateToMatches = { sportId ->
                        globalSportId = sportId
                        navController.currentBackStackEntry?.savedStateHandle?.set(
                            "selectedDate",
                            LocalDate.now().toString()
                        )
                        navController.navigate(Screen.Matches.createRoute(sportId))
                    },
                    onNavigateToProfile = {
                        navController.navigate(Screen.Profile.route)
                    },
                    onNavigateToPlayer = { eventId, bgUrl ->
                        navController.navigate(Screen.Player.createRoute(eventId, bgUrl))
                    },
                )
            }

            composable(Screen.Matches.route) { backStackEntry ->
                val sportIdFromRoute = backStackEntry.arguments?.getString("sport") ?: globalSportId

                val selectedClubId by backStackEntry.savedStateHandle
                    .getStateFlow<String?>("selectedClub", null)
                    .collectAsStateWithLifecycle()

                val savedDateString by backStackEntry.savedStateHandle
                    .getStateFlow<String?>("selectedDate", LocalDate.now().toString())
                    .collectAsStateWithLifecycle()

                MatchesScreen(
                    initialSport = if (globalSportId.isNotEmpty()) globalSportId else sportIdFromRoute,
                    selectedRegion = globalRegion,
                    filterClubId = selectedClubId,
                    apiService = apiService,
                    sports = globalSports,
                    onSportSelected = { globalSportId = it },
                    initialDate = LocalDate.parse(savedDateString),
                    onDateSelected = { newDate ->
                        backStackEntry.savedStateHandle["selectedDate"] = newDate.toString()
                    },
                    onNavigateToClubSelection = {
                        navController.navigate(Screen.ClubSelection.route)
                    },
                    onClearClubFilter = {
                        backStackEntry.savedStateHandle["selectedClub"] = null
                    },
                    onNavigateToProfile = {
                        navController.navigate(Screen.Profile.route)
                    },
                    onNavigateToPlayer = { eventId, backgroundImage ->
                        navController.navigate(Screen.Player.createRoute(eventId, backgroundImage))
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
                    apiService = apiService,
                    onNavigateToLogin = {
                        navController.navigate(Screen.Linking.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = Screen.Player.route,
                arguments = listOf(
                    androidx.navigation.navArgument("eventId") {
                        type = androidx.navigation.NavType.StringType
                    },
                    androidx.navigation.navArgument("backgroundImage") {
                        type = androidx.navigation.NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                ),
                deepLinks = listOf(
                    androidx.navigation.navDeepLink {
                        uriPattern = "eivotv://player/{eventId}"
                    }
                )
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                val backgroundImage = backStackEntry.arguments?.getString("backgroundImage")

                PlayerScreen(
                    eventId = eventId,
                    backgroundImage = backgroundImage,
                    apiService = apiService,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        if (showOverlayPermissionDialog) {
            ConfirmationDialog(
                title = "Para que el reproductor pueda abrirse automáticamente al recibir una notificación con la app en segundo plano, activa el permiso \"Mostrar sobre otras apps\" en los ajustes del sistema.",
                confirmText = "Activar",
                dismissText = "Ahora no",
                onConfirm = {
                    showOverlayPermissionDialog = false
                    onRequestOverlayPermission()
                },
                onDismiss = { showOverlayPermissionDialog = false }
            )
        }
    }
}
