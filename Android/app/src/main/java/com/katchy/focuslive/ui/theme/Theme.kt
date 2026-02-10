package com.katchy.focuslive.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import android.os.Build

private val LightColorScheme = lightColorScheme(
    primary = AntiPrimary,
    onPrimary = Color.White,
    secondary = AntiAccent,
    onSecondary = Color.White,
    background = AntiFlashWhite,
    onBackground = AntiTextPrimary,
    surface = AntiWhite,
    onSurface = AntiTextPrimary,
    surfaceVariant = Color(0xFFF3F4F6),
    onSurfaceVariant = AntiTextSecondary,
    error = AntiError
)

private val DarkColorScheme = darkColorScheme(
    primary = AntiAccent,
    onPrimary = Color.Black,
    secondary = AntiPrimary,
    onSecondary = Color.White,
    background = Color(0xFF0D1117), // Main Dark Background (GitHub Dimmed style)
    onBackground = Color(0xFFF0F6FC), // High contrast text
    surface = Color(0xFF161B22), // Cards background
    onSurface = Color(0xFFF0F6FC), // Text on cards
    surfaceVariant = Color(0xFF21262D), // Borders / Secondary backgrounds
    onSurfaceVariant = Color(0xFF8B949E), // Secondary text
    error = AntiError
)

@Composable
fun BrishTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true, // Material You support
    accentColor: Int? = null, // Custom accent support
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    
    val baseScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && accentColor == null -> {
            if (darkTheme) androidx.compose.material3.dynamicDarkColorScheme(context) else androidx.compose.material3.dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    // Apply custom accent if present
    val finalScheme = if (accentColor != null) {
        val accent = Color(accentColor)
        baseScheme.copy(
            primary = accent,
            secondary = accent,
            tertiary = accent, // unify accent
            onPrimary = Color.White,
            primaryContainer = accent.copy(alpha = 0.15f),
            onPrimaryContainer = accent
        )
    } else {
        baseScheme
    }

    MaterialTheme(
        colorScheme = finalScheme,
        typography = Typography,
        content = content
    )
}