package com.example.eivo_androidtv.ui.screens.club

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.tv.material3.*
import com.example.eivo_androidtv.R
import com.example.eivo_androidtv.data.network.MockApiService.getClubes
import com.example.eivo_androidtv.domain.model.ClubData
import com.example.eivo_androidtv.ui.components.ProfileIcon
import com.example.eivo_androidtv.ui.components.BackgroundFoci


@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ClubSelectionScreen(
    selectedRegion: String,
    onClubSelected: (String) -> Unit,
    onBackWithoutSelection: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val clubs = remember(selectedRegion) {
        getClubes(selectedRegion)
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        BackgroundFoci(Color(0xFF269091))

        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 48.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Image(painterResource(id = R.drawable.ic_eivo_logo), null, Modifier.width(80.dp))
                ProfileIcon(onNavigateToProfile = onNavigateToProfile)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                var isBackFocused by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier
                        .onFocusChanged { isBackFocused = it.isFocused }
                        .border(
                            width = 2.dp,
                            color = if (isBackFocused) Color.White else Color.Transparent,
                            shape = RectangleShape
                        )
                        .padding(4.dp)
                ) {
                    Surface(
                        onClick = onBackWithoutSelection,
                        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
                        shape = ClickableSurfaceDefaults.shape(RectangleShape),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = Color(0xFFC0C0C0),
                            focusedContainerColor = Color(0xFFC0C0C0)
                        )
                    ) {
                        Text(
                            text = "Volver sin escoger club",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalArrangement = Arrangement.spacedBy(40.dp),
                contentPadding = PaddingValues(top = 20.dp, bottom = 48.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(clubs) { club ->
                    ClubSelectionItem(club = club, onClick = { onClubSelected(club.id) })
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun ClubSelectionItem(club: ClubData, onClick: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .width(140.dp)
            .zIndex(if (isFocused) 1f else 0f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            onClick = onClick,
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
            shape = ClickableSurfaceDefaults.shape(RectangleShape),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .onFocusChanged { isFocused = it.isFocused }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 2.dp,
                        color = if (isFocused) Color.White else Color.Transparent,
                        shape = RectangleShape
                    )
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = club.logoRes),
                    contentDescription = club.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = club.name.uppercase(),
            color = Color.White,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}