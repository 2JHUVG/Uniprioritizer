package com.example.uniprioritizer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.uniprioritizer.domain.model.ThemeMode

private val LightColorScheme = lightColorScheme(
    primary = Forest40,
    onPrimary = Color.White,
    primaryContainer = Forest90,
    onPrimaryContainer = Forest10,
    secondary = Sage40,
    onSecondary = Color.White,
    secondaryContainer = Sage90,
    onSecondaryContainer = Forest10,
    tertiary = Forest10,
    onTertiary = Color.White,
    tertiaryContainer = Mist80,
    onTertiaryContainer = Forest10,
    background = Paper,
    onBackground = Forest10,
    surface = Paper,
    onSurface = Forest10,
    surfaceVariant = Sage90,
    onSurfaceVariant = Sage50,
    errorContainer = Amber90,
    onErrorContainer = Amber40
)

private val DarkColorScheme = darkColorScheme(
    primary = Forest80,
    onPrimary = Forest10,
    primaryContainer = Forest30,
    onPrimaryContainer = Forest90,
    secondary = Sage80,
    onSecondary = Forest10,
    secondaryContainer = Sage25,
    onSecondaryContainer = Sage90,
    tertiary = Forest30,
    onTertiary = Forest90,
    tertiaryContainer = Sage50,
    onTertiaryContainer = Forest90,
    background = Night,
    onBackground = Sage90,
    surface = Night,
    onSurface = Sage90,
    surfaceVariant = NightSurface,
    onSurfaceVariant = Sage80,
    errorContainer = Amber20,
    onErrorContainer = Amber80
)

@Composable
fun UniPrioritizerTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
