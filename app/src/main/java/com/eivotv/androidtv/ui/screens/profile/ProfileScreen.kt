package com.eivotv.androidtv.ui.screens.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.eivotv.androidtv.R
import com.eivotv.androidtv.data.local.SessionManager
import com.eivotv.androidtv.data.remote.EivoApiService
import com.eivotv.androidtv.data.remote.dto.ProfileResponseDto
import com.eivotv.androidtv.data.remote.dto.SuscripcionDto
import com.eivotv.androidtv.data.remote.dto.UserDataDto
import com.eivotv.androidtv.ui.components.EivoScreenContainer
import com.eivotv.androidtv.ui.components.ErrorStateComponent
import kotlinx.coroutines.launch
import com.eivotv.androidtv.ui.components.ConfirmationDialog

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToLogin: () -> Unit,
    sessionManager: SessionManager,
    apiService: EivoApiService
) {
    var profileData by remember { mutableStateOf<ProfileResponseDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorOccurred by remember { mutableStateOf(false) }
    var isOffline by remember { mutableStateOf(false) }
    var showUnlinkDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val loadProfile = suspend {
        isLoading = true
        errorOccurred = false
        isOffline = false
        try {
            profileData = apiService.fetchUserProfile()
        } catch (e: java.net.UnknownHostException) {
            isOffline = true
            errorOccurred = true
        } catch (e: Exception) {
            errorOccurred = true
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadProfile()
    }

    if (showUnlinkDialog) {
        ConfirmationDialog(
            title = stringResource(R.string.unlink_confirmation_title),
            onConfirm = {
                sessionManager.clearSession()
                onNavigateToLogin()
            },
            onDismiss = { showUnlinkDialog = false }
        )
    } else {
        EivoScreenContainer {
            when {
                isLoading -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        androidx.compose.material.CircularProgressIndicator(color = Color.White)
                    }
                }

                errorOccurred -> {
                    ErrorStateComponent(
                        isOffline = isOffline,
                        onRetry = {
                            scope.launch { loadProfile() }
                        }
                    )
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 64.dp, vertical = 32.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_eivo_logo),
                            contentDescription = null,
                            modifier = Modifier.width(90.dp)
                        )

                        Spacer(modifier = Modifier.height(30.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(60.dp)
                        ) {
                            Column(modifier = Modifier.weight(1.1f)) {
                                SubscriptionCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    user = profileData?.user,
                                    suscripcion = profileData?.suscripciones?.firstOrNull()
                                )
                                Spacer(modifier = Modifier.height(40.dp))

                                Text(
                                    text = stringResource(R.string.profile_unlink_desc),
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(16.dp))


                                var isUnlinkFocused by remember { mutableStateOf(false) }
                                Box(
                                    modifier = Modifier
                                        .onFocusChanged { isUnlinkFocused = it.isFocused }
                                        .border(1.dp, if (isUnlinkFocused) Color.White else Color.Transparent, RectangleShape)
                                        .padding(4.dp)
                                ) {
                                    Surface(
                                        onClick = { showUnlinkDialog = true },
                                        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
                                        shape = ClickableSurfaceDefaults.shape(RectangleShape),
                                        colors = ClickableSurfaceDefaults.colors(
                                            containerColor = Color(0xFF151920),
                                            focusedContainerColor = Color(0xFF151920)
                                        )
                                    ) {
                                        Text(
                                            text = stringResource(R.string.profile_unlink_button),
                                            color = Color.White,
                                            style = MaterialTheme.typography.bodyLarge,
                                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                            StatsSection(
                                modifier = Modifier.weight(0.9f),
                                jornadas = profileData?.jornadas ?: 0,
                                eventos = profileData?.eventos ?: 0
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatsSection(
    modifier: Modifier,
    jornadas: Int,
    eventos: Int
) {
    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.profile_available_for_me),
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White))
        StatRow(stringResource(R.string.profile_stat_matchdays), jornadas.toString())
        StatRow(stringResource(R.string.profile_stat_summaries), eventos.toString())
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SubscriptionCard(
    modifier: Modifier,
    user: UserDataDto?,
    suscripcion: SuscripcionDto?
) {
    Box(
        modifier = modifier
            .background(Color(0xFF1B1F27), RoundedCornerShape(8.dp))
            .padding(20.dp)
    ) {
        Column {
            // Cabecera (Título y Email)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.profile_title),
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = user?.email ?: "",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White))
            Spacer(modifier = Modifier.height(16.dp))

            // Contenido Principal
            Row(
                modifier = Modifier.fillMaxWidth(),

                verticalAlignment = Alignment.Top
            ) {
                // Columna de Textos de suscripción
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = suscripcion?.nombre?.uppercase() ?: stringResource(R.string.profile_no_subscription),
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = suscripcion?.club ?: "",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = suscripcion?.categoria ?: "",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .offset(y = (-10).dp)
                ) {
                    AsyncImage(
                        model = suscripcion?.escudo,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // Fecha "Desde el..."
            val fechaInicio = user?.start_sub_date?.substringBefore(" ") ?: stringResource(R.string.no_available)
            Text(
                text = "Desde el $fechaInicio",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = value,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White))
    }
}