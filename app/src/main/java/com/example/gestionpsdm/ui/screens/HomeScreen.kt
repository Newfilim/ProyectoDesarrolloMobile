
package com.example.gestionpsdm.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "Junta de vecinos Zaror",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Bienvenido a nuestra comunidad",
            style = MaterialTheme.typography.bodyLarge
        )

        TarjetaEspacios(onVerEspacios)

        TarjetaFinanciera(onVerFinanzas)

        Button(
            onClick = onVerMisReservas
        ) {
            Text("Mis reservas")
        }

        Button(
            onClick = onCerrarSesion
        ) {
            Text("Cerrar sesión")
        }
    }
}

@Composable
fun TarjetaEspacios(
    onVerEspacios: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Text(
                text = "Espacios comunitarios",
                style = MaterialTheme.typography.titleMedium
            )

            Text("Consulta los espacios disponibles y solicita una reserva")

            Button(
                onClick = onVerEspacios
            ) {
                Text("Ver espacios disponibles")
            }
        }
    }
}

@Composable
fun TarjetaFinanciera(
    onVerFinanzas: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Text(
                text = "Transparencia financiera",
                style = MaterialTheme.typography.titleMedium
            )

            Text("Consulta los ingresos y gastos de la comunidad")

            Button(
                onClick = onVerFinanzas
            ) {
                Text("Ver finanzas")
            }
        }
    }
}
