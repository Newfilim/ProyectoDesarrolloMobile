
package com.example.gestionpsdm.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Surface
import androidx.compose.ui.text.style.TextAlign

@Composable
fun HomeScreen(
    onVerEspacios: () -> Unit,
    onVerFinanzas: () -> Unit,
    onVerMisReservas: () -> Unit,
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {


// Encabezado de bienvenida
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {

            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Text(
                    text = "JUNTA DE VECINOS ZAROR",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "¡Bienvenido a tu comunidad!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Desde aquí puedes reservar espacios comunitarios, revisar tus solicitudes y consultar las finanzas.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

// Título para las acciones
        Text(
            text = "¿Qué deseas hacer hoy?",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth()
        )


        TarjetaEspacios(onVerEspacios)

        TarjetaAccionResidente(
            titulo = "Mis reservas",
            descripcion = "Consulta tus solicitudes, revisa su estado y adjunta tus comprobantes de pago.",
            textoBoton = "Ver mis reservas",
            onClick = onVerMisReservas
        )

        TarjetaFinanciera(onVerFinanzas)
    }
}

@Composable
fun TarjetaEspacios(
    onVerEspacios: () -> Unit
) {

    TarjetaAccionResidente(
        titulo = "Espacios comunitarios",
        descripcion = "Conoce el quincho, la piscina y la sala multiuso. Revisa cuáles están disponibles y solicita una reserva.",
        textoBoton = "Ver espacios",
        onClick = onVerEspacios
    )
}

@Composable
fun TarjetaFinanciera(
    onVerFinanzas: () -> Unit
) {

    TarjetaAccionResidente(
        titulo = "Transparencia financiera",
        descripcion = "Consulta la información sobre los ingresos y gastos de nuestra comunidad.",
        textoBoton = "Ver finanzas",
        onClick = onVerFinanzas
    )
}

@Composable
private fun TarjetaAccionResidente(
    titulo: String,
    descripcion: String,
    textoBoton: String,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = descripcion,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "$textoBoton  →",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
