package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    secondary = GoldAccent,
    tertiary = SuccessGreen,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = LightSurface,
    onSecondary = DarkBackground,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
    primaryContainer = EmeraldPrimaryVariant,
    secondaryContainer = DarkSurface
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    secondary = GoldAccent,
    tertiary = SuccessGreen,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = LightSurface,
    onSecondary = LightSurface,
    onBackground = LightOnBackground,
    onSurface = LightOnSurface,
    primaryContainer = MintHighlight,
    secondaryContainer = LightBackground
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // We can allow users to toggle dynamic coloring: set default to false for a strict beautiful green-gold aesthetic,
    // which highlights the serene mosque vibe.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
