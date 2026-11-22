package com.example.litutur.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = CoralOrangePrimary,
    onPrimary = CardContainerSurface,
    primaryContainer = SubtleHighlightOrange,
    onPrimaryContainer = CoralOrangeDark,

    secondary = SecondaryGreenAccent,
    onSecondary = CardContainerSurface,
    secondaryContainer = SubtleHighlightGreen,
    onSecondaryContainer = SecondaryBrandGreen,

    tertiary = InfoAnalyticsBlue,
    onTertiary = CardContainerSurface,
    tertiaryContainer = SubtleHighlightBlue,
    onTertiaryContainer = InfoAnalyticsBlue,

    background = ScreenBaseCanvas,
    onBackground = TextPrimarySlate,

    surface = CardContainerSurface,
    onSurface = TextPrimarySlate,
    surfaceVariant = ScreenBaseCanvas,
    onSurfaceVariant = TextSecondarySlate,

    outline = BorderDividerColor,
    outlineVariant = TextMutedSlate,

    error = AlertDangerCoral,
    onError = CardContainerSurface,
    errorContainer = AlertDangerCoralLight,
    onErrorContainer = AlertDangerCoral
)

private val DarkColorScheme = darkColorScheme(
    primary = CoralOrangeAccent,
    onPrimary = TextPrimarySlate,
    primaryContainer = CoralOrangeDark,
    onPrimaryContainer = SubtleHighlightOrange,

    secondary = SecondaryBrandGreen,
    onSecondary = TextPrimarySlate,
    secondaryContainer = SecondaryGreenAccent,
    onSecondaryContainer = SubtleHighlightGreen,

    tertiary = InfoAnalyticsBlueLight,
    onTertiary = TextPrimarySlate,

    background = TextPrimarySlate,
    onBackground = ScreenBaseCanvas,

    surface = TextPrimarySlate,
    onSurface = ScreenBaseCanvas,
    surfaceVariant = TextSecondarySlate,
    onSurfaceVariant = BorderDividerColor,

    outline = TextMutedSlate,
    error = AlertDangerCoralLight
)

@Composable
fun LituturTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Selalu gunakan LightColorScheme agar tampilan konsisten dan teks terbaca jelas
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = true
            insetsController.isAppearanceLightNavigationBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
