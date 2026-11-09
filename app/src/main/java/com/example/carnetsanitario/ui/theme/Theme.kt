package com.example.carnetsanitario.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CelestePrimario,
    secondary = RosaAcento,
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = CelesteOscuro,
    secondary = RosaOscuro,
    tertiary = CelestePrimario,
    background = FondoPantalla,
    surface = SuperficieTarjeta,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextoPrimario,
    onSurface = TextoPrimario
)

@Composable
fun CarnetSanitarioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // false para conservar la paleta visual definida
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}