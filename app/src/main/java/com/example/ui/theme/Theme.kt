package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFA1B8EA),
    primaryContainer = Color(0xFF2B4678),
    secondary = BentoPrimaryContainer,
    tertiary = BentoSuccess,
    background = BentoDarkCard,
    surface = Color(0xFF25262B),
    surfaceVariant = Color(0xFF32343A),
    onPrimary = Color(0xFF0F172A),
    onSecondary = BentoTextPrimary,
    onBackground = BentoOnDarkCard,
    onSurface = BentoOnDarkCard
)

private val LightColorScheme = lightColorScheme(
    primary = BentoPrimary,
    primaryContainer = BentoPrimaryContainer,
    onPrimaryContainer = BentoOnPrimaryContainer,
    secondary = Color(0xFF5B75A8),
    tertiary = BentoSuccess,
    background = BentoBackground,
    surface = BentoSurface,
    surfaceVariant = BentoSurfaceVariant,
    outline = BentoBorder,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = BentoTextPrimary,
    onSurface = BentoTextPrimary
)

@Composable
fun AttendEaseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use intentional theme colors
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

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    AttendEaseTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
