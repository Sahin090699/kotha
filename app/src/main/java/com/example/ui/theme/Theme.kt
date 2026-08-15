package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val CleanMinimalColorScheme = lightColorScheme(
    primary = CleanDeepNavy,
    onPrimary = Color.White,
    primaryContainer = CleanPillBlue,
    onPrimaryContainer = CleanDeepNavy,
    secondary = CleanBengaliAccent,
    onSecondary = Color.White,
    secondaryContainer = CleanPillGrey,
    onSecondaryContainer = CleanTextPrimary,
    tertiary = CleanRecordingPulse,
    onTertiary = Color.White,
    tertiaryContainer = CleanEndCallContainer,
    onTertiaryContainer = CleanEndCallText,
    background = CleanBackground,
    onBackground = CleanTextPrimary,
    surface = CleanSurface,
    onSurface = CleanTextPrimary,
    surfaceVariant = CleanSurfaceVariant,
    onSurfaceVariant = CleanTextSecondary,
    outline = CleanBorder,
    outlineVariant = CleanDivider
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = CleanMinimalColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

