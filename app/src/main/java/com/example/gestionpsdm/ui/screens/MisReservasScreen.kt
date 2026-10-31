
package com.example.gestionpsdm.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gestionpsdm.model.EstadoReserva
import com.example.gestionpsdm.model.Reserva

@Composable
fun MisReservasScreen(
    modifier: Modifier = Modifier
) {

    val reservas = listOf(
        Reserva(
            id = 1,
            idUsuario = 1,
            idEspacio = 1,
            fecha = "06/11/2026",
            horaInicio = "19:00",
            horaFin = "22:00",
            estado = EstadoReserva.PENDIENTE
        ),
        Reserva(
            id = 2,
            idUsuario = 1,
            idEspacio = 2,
            fecha = "07/11/2026",
            horaInicio = "10:00",
            horaFin = "14:00",
            estado = EstadoReserva.APROBADA
        )
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Mis reservas",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        item {
            Text("Consulta el estado de tus solicitudes.")
        }

        items(reservas) { reserva ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Reserva N.º ${reserva.id}",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text("Espacio ID: ${reserva.idEspacio}")
                    Text("Fecha: ${reserva.fecha}")
                    Text("Desde: ${reserva.horaInicio}")
                    Text("Hasta: ${reserva.horaFin}")
                    Text("Estado: ${reserva.estado.name}")
                }
            }
        }
    }
}
