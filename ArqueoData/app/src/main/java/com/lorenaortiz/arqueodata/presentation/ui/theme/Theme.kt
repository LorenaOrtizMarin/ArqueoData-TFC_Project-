package com.lorenaortiz.arqueodata.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryColor,
    secondary = SecondaryColor,
    tertiary = ButtonText,
    surface = Details,
    onSurface = MainText,
    onPrimary = ButtonText,
    onSecondary = ButtonText,
    outline = Outline,
    onSurfaceVariant = SecondaryText,
    surfaceVariant = Details,
    surfaceTint = PrimaryColor
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    secondary = SecondaryColor,
    tertiary = ButtonText,
    background = Color.White,
    onBackground = MainText,
    surface = Color.White,
    onSurface = MainText,
    onPrimary = ButtonText,
    onSecondary = ButtonText,
    outline = Outline,
    onSurfaceVariant = SecondaryText,
    surfaceVariant = Color.White,
    surfaceTint = PrimaryColor
)

@Composable
fun ArqueoDataTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Desactivamos los colores dinámicos por defecto
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
            window.statusBarColor = colorScheme.primary.toArgb()
            window.navigationBarColor = colorScheme.primary.toArgb()
            
            // Las siguientes líneas manejan la apariencia de los íconos de la barra de estado y navegación.
            // isAppearanceLightStatusBars = !darkTheme significa que los íconos serán oscuros en modo claro y claros en modo oscuro.
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            window.navigationBarDividerColor = colorScheme.primary.toArgb()
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}