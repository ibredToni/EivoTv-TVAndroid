package com.example.eivo_androidtv.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.example.eivo_androidtv.R

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ErrorStateComponent(
    isOffline: Boolean,
    onRetry: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val barHeight = 42.dp // La misma altura que usamos en la barra de filtros

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(
                id = if (isOffline) R.drawable.ic_no_internet else R.drawable.ic_error
            ),
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color.White.copy(alpha = 0.5f)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = if (isOffline) stringResource(R.string.error_no_internet) else stringResource(R.string.error_something_wrong),
            color = Color.White,
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp)
        )
        Text(
            text = if (isOffline) stringResource(R.string.error_check_network) else stringResource(R.string.error_content_load_failed),
            color = Color.White.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
        )
        Spacer(Modifier.height(32.dp))

        // Aplicamos la altura de 42.dp para que coincida con los de "Ver Directo"
        Box(modifier = Modifier
            .focusRequester(focusRequester)
            .height(barHeight)
        ) {
            FilterButton(
                text = stringResource(R.string.retry),
                isActive = false,
                onClick = onRetry,
                modifier = Modifier.fillMaxHeight() // Esto hará que respete los 42.dp del Box
            )
        }
    }
}