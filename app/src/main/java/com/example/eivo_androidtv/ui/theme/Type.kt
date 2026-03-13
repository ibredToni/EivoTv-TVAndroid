package com.example.eivo_androidtv.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Typography
import com.example.eivo_androidtv.R

// 1. Definimos la nueva familia Saira Condensed
val SairaCondensedFontFamily = FontFamily(
    Font(R.font.sairaextracondensed_regular, FontWeight.Normal),
    //Font(R.font.sairasemicondensed_semibold, FontWeight.SemiBold),
    Font(R.font.sairaextracondensed_bold, FontWeight.Bold)
)

@OptIn(ExperimentalTvMaterial3Api::class)
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = SairaCondensedFontFamily, // Cambiado aquí
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = SairaCondensedFontFamily, // Cambiado aquí
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    // Puedes usar SemiBold en algún estilo específico si quieres probarlo
    labelSmall = TextStyle(
        fontFamily = SairaCondensedFontFamily,
        fontWeight = FontWeight.SemiBold, // Probando el SemiBold aquí
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)