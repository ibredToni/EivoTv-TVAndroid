package com.example.eivo_androidtv.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Typography
import com.example.eivo_androidtv.R

// --- Familias de Fuentes ---

val Saira = FontFamily(
    Font(R.font.saira_regular, FontWeight.Normal),
    Font(R.font.saira_bold, FontWeight.Bold)
)

val SairaCondensed = FontFamily(
    Font(R.font.sairacondensed_regular, FontWeight.Normal),
    Font(R.font.sairacondensed_semibold, FontWeight.SemiBold),
)

val SairaSemiCondensed = FontFamily(
    Font(R.font.sairasemicondensed_regular, FontWeight.Normal),
    Font(R.font.sairasemicondensed_semibold, FontWeight.SemiBold),
    Font(R.font.sairasemicondensed_bold, FontWeight.Bold)
)

val SairaExtraCondensed = FontFamily(
    Font(R.font.sairaextracondensed_regular, FontWeight.Normal),
    Font(R.font.sairaextracondensed_bold, FontWeight.Bold)
)

// --- Sistema de Tipografía ---

@OptIn(ExperimentalTvMaterial3Api::class)
val Typography = Typography(
    // Títulos Gigantes (Pause Screen)
    displayMedium = TextStyle(
        fontFamily = SairaExtraCondensed,
        fontWeight = FontWeight.Bold,
        fontSize = 35.sp,
        lineHeight = 40.sp
    ),
    //Botones
    displaySmall = TextStyle(
        fontFamily = SairaCondensed,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 20.sp
    ),
    // Títulos de Cards (MatchCard)
    headlineSmall = TextStyle(
        fontFamily = SairaExtraCondensed,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 18.sp
    ),
    // Textos
    headlineMedium = TextStyle(
        fontFamily = SairaExtraCondensed,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp
    ),
    // Botones de Filtro
    titleSmall = TextStyle(
        fontFamily = SairaSemiCondensed,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        textAlign = TextAlign.Center
    ),
    // Cuerpo de texto Destacado/Bold (Descripción MatchCard)
    bodyMedium = TextStyle(
        fontFamily = SairaSemiCondensed,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp
    ),
    // Cuerpo de texto
    bodyLarge = TextStyle(
        fontFamily = SairaSemiCondensed,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    // Cuerpo de texto Normal (Fecha/Hora, Descripciones secundarias)
    bodySmall = TextStyle(
        fontFamily = SairaSemiCondensed,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    ),
    // Etiquetas de Publicidad (AD)
    labelLarge = TextStyle(
        fontFamily = Saira,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 16.sp
    ),
    // Textos mínimos (Botón "Seguir viendo")
    labelSmall = TextStyle(
        fontFamily = Saira,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        lineHeight = 10.sp
    ),
    // Textos medios
    labelMedium = TextStyle(
        fontFamily = Saira,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 14.sp
    )

)