package com.example.e_clinic_app.ui.theme

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
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)
/**
 * Provides the theme configuration for the e-clinic application.
 *
 * This file defines the light and dark color schemes, as well as dynamic color support
 * for devices running Android 12 (API level 31) or higher. The theme is applied using
 * Material 3 design principles to ensure a consistent and modern look across the app.
 *
 * The theme includes:
 * - Light and dark color schemes with primary, secondary, and tertiary colors.
 * - Dynamic color support for adapting to the system's wallpaper-based color palette.
 * - Integration with Material 3's `MaterialTheme` for typography and color scheme.
 *
 * @param darkTheme A boolean indicating whether the app should use the dark theme. Defaults to the system's dark theme setting.
 * @param dynamicColor A boolean indicating whether to use dynamic colors (available on Android 12+). Defaults to true.
 * @param content A composable lambda that represents the UI content to which the theme will be applied.
 */
@Composable
fun EClinic_AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}