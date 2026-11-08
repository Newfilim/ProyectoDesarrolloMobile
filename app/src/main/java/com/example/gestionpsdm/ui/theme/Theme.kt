
package com.example.gestionpsdm.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(

    primary = LavandaPrincipal,
    onPrimary = TextoSobreLavanda,

    primaryContainer = SuperficieElevada,
    onPrimaryContainer = TextoPrincipal,

    secondary = VioletaSecundario,
    onSecondary = TextoSobreLavanda,

    secondaryContainer = TarjetaOscura,
    onSecondaryContainer = TextoPrincipal,

    background = FondoOscuro,
    onBackground = TextoPrincipal,

    surface = SuperficieOscura,
    onSurface = TextoPrincipal,

    surfaceVariant = TarjetaOscura,
    onSurfaceVariant = TextoSecundario,

    surfaceContainerLow = SuperficieOscura,
    surfaceContainer = TarjetaOscura,
    surfaceContainerHigh = SuperficieElevada,

    outline = VioletaSecundario,

    error = ErrorSuave,
    onError = TextoSobreLavanda
)

@Composable
fun GestionPSDMTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
