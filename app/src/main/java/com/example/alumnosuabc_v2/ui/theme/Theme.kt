package com.example.alumnosuabc_v2.ui.theme

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
    primary = VerdeOscuroPrimario,
    onPrimary = VerdeOscuroSobrePrimario,
    primaryContainer = VerdeOscuroContenedor,
    onPrimaryContainer = VerdeOscuroSobreContenedor,
    secondary = VerdeOscuroPrimario,
    onSecondary = VerdeOscuroSobrePrimario,
    secondaryContainer = VerdeOscuroContenedor,
    onSecondaryContainer = VerdeOscuroSobreContenedor,
    background = VerdeOscuroFondo,
    onBackground = VerdeOscuroTextoPrincipal,
    surface = VerdeOscuroSuperficie,
    onSurface = VerdeOscuroTextoPrincipal,
    surfaceVariant = VerdeOscuroSuperficieVariante,
    onSurfaceVariant = VerdeOscuroTextoSecundario,
    surfaceContainer = VerdeOscuroSuperficie,
    surfaceContainerLow = VerdeOscuroFondo
)

private val LightColorScheme = lightColorScheme(
    primary = VerdePrimario,
    onPrimary = VerdeSobrePrimario,
    primaryContainer = VerdeContenedor,
    onPrimaryContainer = VerdeSobreContenedor,
    secondary = VerdePrimario,
    onSecondary = VerdeSobrePrimario,
    secondaryContainer = VerdeContenedor,
    onSecondaryContainer = VerdeSobreContenedor,
    background = VerdeFondo,
    onBackground = VerdeTextoPrincipal,
    surface = VerdeSuperficie,
    onSurface = VerdeTextoPrincipal,
    surfaceVariant = VerdeSuperficieVariante,
    onSurfaceVariant = VerdeTextoSecundario,
    surfaceContainer = VerdeSuperficie,
    surfaceContainerLow = VerdeFondo
)

@Composable
fun AlumnosUABC_V2Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
