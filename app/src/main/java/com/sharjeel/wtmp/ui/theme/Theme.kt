package com.sharjeel.wtmp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = SapphireBlue,
    onPrimary = Color.White,
    primaryContainer = SapphireBlue.copy(alpha = 0.1f),
    onPrimaryContainer = SapphireBlue,

    secondary = TitaniumGray,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF1F5F9),
    onSecondaryContainer = TitaniumGray,

    tertiary = CyberCyan,
    onTertiary = Color.White,
    tertiaryContainer = CyberCyan.copy(alpha = 0.1f),
    onTertiaryContainer = Color(0xFF0E7490),

    background = GhostWhite,
    onBackground = LightTextPrimary,

    surface = Color.White,
    onSurface = LightTextPrimary,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = LightTextSecondary,

    outline = Color(0xFFCBD5E1),
    error = AlertRose,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF60A5FA), // Brightened Sapphire for Dark Mode
    onPrimary = OnyxBlack,
    primaryContainer = SapphireBlue.copy(alpha = 0.3f),
    onPrimaryContainer = Color.White,

    secondary = Color(0xFF94A3B8),
    onSecondary = OnyxBlack,
    secondaryContainer = Color(0xFF334155),
    onSecondaryContainer = Color(0xFFF1F5F9),

    tertiary = Color(0xFF22D3EE),
    onTertiary = OnyxBlack,
    tertiaryContainer = Color(0xFF155E75),
    onTertiaryContainer = Color(0xFFCFFAFE),

    background = OnyxBlack,
    onBackground = DarkTextPrimary,

    surface = DarkSurfaceElevated,
    onSurface = DarkTextPrimary,
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = DarkTextSecondary,

    outline = Color(0xFF475569),
    error = Color(0xFFFB7185),
    onError = OnyxBlack
)

@Composable
fun WTMPTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Standardizing branding for professional consistency
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
