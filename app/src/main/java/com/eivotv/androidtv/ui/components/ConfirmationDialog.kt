package com.eivotv.androidtv.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.*
import com.eivotv.androidtv.R

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ConfirmationDialog(
    title: String,
    confirmText: String = stringResource(R.string.yes),
    dismissText: String = stringResource(R.string.no),
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            Image(
                painter = painterResource(id = R.drawable.fondo_estadio),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.7f
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_eivo_logo),
                    contentDescription = null,
                    modifier = Modifier.width(150.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = title,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontSize = 22.sp,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(horizontal = 48.dp)
                )

                Spacer(modifier = Modifier.height(40.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    DialogButton(text = confirmText, onClick = onConfirm)

                    DialogButton(
                        text = dismissText,
                        onClick = onDismiss,
                        modifier = Modifier.focusRequester(focusRequester)
                    )
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun DialogButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    var isFocused by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .onFocusChanged { isFocused = it.isFocused }
            .border(2.dp, if (isFocused) Color.White else Color.Transparent, RectangleShape)
            .padding(4.dp)
    ) {
        Surface(
            onClick = onClick,
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
            shape = ClickableSurfaceDefaults.shape(RectangleShape),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = if (isFocused) Color.White else Color(0xFF151920).copy(alpha = 0.8f),
                focusedContainerColor = Color.White
            )
        ) {
            Text(
                text = text,
                color = if (isFocused) Color.Black else Color.White,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 2.dp)
            )
        }
    }
}