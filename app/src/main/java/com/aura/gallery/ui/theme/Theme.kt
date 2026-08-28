package com.aura.gallery.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = iOSBlue,
    background = DarkBg,
    surface = DarkSurface,
    onBackground = DarkText,
    onSurface = DarkText,
    error = iOSRed
)

private val LightColors = lightColorScheme(
    primary = iOSBlue,
    background = LightBg,
    surface = LightSurface,
    onBackground = LightText,
    onSurface = LightText,
    error = iOSRed
)

/** themePref: 0 = follow system, 1 = light, 2 = dark. */
@Composable
fun AuraGalleryTheme(themePref: Int = 0, content: @Composable () -> Unit) {
    val dark = when (themePref) {
        1 -> false
        2 -> true
        else -> isSystemInDarkTheme()
    }
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        content = content
    )
}
