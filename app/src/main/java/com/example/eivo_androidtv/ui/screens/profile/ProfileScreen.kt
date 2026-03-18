package com.example.eivo_androidtv.ui.screens.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.example.eivo_androidtv.R
import com.example.eivo_androidtv.data.local.SessionManager
import com.example.eivo_androidtv.data.remote.EivoApiService
import com.example.eivo_androidtv.data.remote.dto.ProfileResponseDto
import com.example.eivo_androidtv.data.remote.dto.SuscripcionDto
import com.example.eivo_androidtv.data.remote.dto.UserDataDto
import com.example.eivo_androidtv.ui.components.EivoScreenContainer
import com.example.eivo_androidtv.ui.components.ErrorStateComponent
import kotlinx.coroutines.launch
import com.example.eivo_androidtv.ui.components.ConfirmationDialog

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
                                    stringResource(R.string.profile_unlink_desc),
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                var isUnlinkFocused by remember { mutableStateOf(false) }
                                Box(
                                    modifier = Modifier
                                        .onFocusChanged { isUnlinkFocused = it.isFocused }
                                        .border(2.dp, if (isUnlinkFocused) Color.White else Color.Transparent, RectangleShape)
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
                                            stringResource(R.string.profile_unlink_button),
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
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
        Text(stringResource(R.string.profile_available_for_me), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(16.dp))
        StatRow(stringResource(R.string.profile_stat_matchdays), jornadas.toString())
        StatRow(stringResource(R.string.profile_stat_summaries), eventos.toString())
    }
}

@Composable
fun SubscriptionCard(
    modifier: Modifier,
    user: UserDataDto?,
    suscripcion: SuscripcionDto?) {
    Box(
        modifier = modifier
            .background(Color(0xFF1B1F27), RoundedCornerShape(8.dp))
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.profile_title), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(user?.email ?: "", color = Color.White, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White))
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        suscripcion?.nombre?.uppercase() ?: stringResource(R.string.profile_no_subscription),
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        suscripcion?.club ?: "",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Text(
                        suscripcion?.categoria ?: "",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(120.dp))
            }

            Spacer(modifier = Modifier.height(42.dp))
            val fechaInicio = user?.start_sub_date?.substringBefore(" ") ?: stringResource(R.string.no_available)
            Text(
                "Desde el $fechaInicio",
                color = Color.White,
                fontSize = 11.sp
            )
        }

        AsyncImage(
            model = suscripcion?.escudo,
            contentDescription = null,
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.BottomEnd)
                .padding(top = 28.dp, start = 20.dp),
            error = null,
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = Color.White, fontSize = 14.sp)
            Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.05f)))
    }
}
