package com.example.eivo_androidtv.ui.screens.linking

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.example.eivo_androidtv.R
import com.example.eivo_androidtv.ui.components.QrImage
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import com.example.eivo_androidtv.ui.components.ErrorStateComponent
import com.example.eivo_androidtv.ui.components.FilterButton

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun LoginLinkingScreen(
    onNavigateToHome: () -> Unit,
    viewModel: LinkingViewModel
) {
    val state = viewModel.uiState
    val colorScheme = MaterialTheme.colorScheme
    val eivoColor = colorScheme.primary
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        viewModel.loadQrAndListen()
    }

    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) {
            onNavigateToHome()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Image(
            painter = painterResource(id = R.drawable.aficion),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_eivo_logo),
                contentDescription = "Eivo Logo",
                modifier = Modifier.fillMaxWidth(0.12f).padding(bottom = 20.dp),
                contentScale = ContentScale.FillWidth
            )

            when {

                state.error != null && !state.isLoading -> {
                    ErrorStateComponent(
                        isOffline = state.error.contains("UnknownHostException", ignoreCase = true),
                        onRetry = { viewModel.loadQrAndListen() }
                    )
                }


                state.isLoading && state.uuid == null -> {
                    Box(modifier = Modifier.size(160.dp), contentAlignment = Alignment.Center) {
                        androidx.compose.material.CircularProgressIndicator(color = eivoColor)
                    }
                }

                state.uuid != null -> {
                    val textStyle = androidx.compose.ui.text.TextStyle(
                        color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, lineHeight = 28.sp
                    )

                    Text(text = stringResource(R.string.linking_qr_instruction), style = textStyle)
                    Spacer(modifier = Modifier.height(24.dp))

                    Box(
                        modifier = Modifier.size(110.dp).background(Color.White, shape = RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        QrImage(text = state.uuid, modifier = Modifier.fillMaxSize())
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    Text(text = stringResource(R.string.linking_manual_instruction), style = textStyle)
                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .width(IntrinsicSize.Max)
                            .background(Color(0xFFBFC8CD).copy(alpha = 0.8f))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.uuid,
                            color = Color.Black,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.Black,
                                fontSize = 14.sp,
                                letterSpacing = 2.sp
                            ),
                            softWrap = true
                        )
                    }

                    Spacer(modifier = Modifier.height(15.dp))

                    Box(modifier = Modifier.focusRequester(focusRequester)) {
                        FilterButton(
                            text = if (state.isLoading) stringResource(R.string.linking_generating) else stringResource(R.string.linking_generate_new),
                            isActive = false,
                            onClick = { viewModel.loadQrAndListen() },
                            fontSize = 14.sp,
                            modifier = Modifier.height(40.dp)
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(state.uuid, state.error) {
        if (state.uuid != null || state.error != null) {
            focusRequester.requestFocus()
        }
    }
        }

