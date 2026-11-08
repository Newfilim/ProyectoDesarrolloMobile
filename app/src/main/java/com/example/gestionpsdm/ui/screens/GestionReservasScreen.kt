
package com.example.gestionpsdm.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gestionpsdm.model.EstadoReserva
import com.example.gestionpsdm.repository.ReservasDemoRepository

@Composable
fun GestionReservasScreen(
    modifier: Modifier = Modifier
) {

    val reservas = ReservasDemoRepository.reservas

    var idReservaConError by remember {
        mutableStateOf<Int?>(null)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        item {
            Text(
                text = "Gestión de reservas",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        item {
            Text("Revisa las solicitudes de los residentes.")
        }

        items(
            items = reservas,
            key = { reserva -> reserva.id }
        ) { reserva ->

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

                    Text("Usuario ID: ${reserva.idUsuario}")
                    Text("Espacio ID: ${reserva.idEspacio}")
                    Text("Fecha: ${reserva.fecha}")

                    Text(
                        text = "Horario: ${reserva.horaInicio} - ${reserva.horaFin}"
                    )

                    Text("Estado: ${reserva.estado.name}")

                    if (reserva.estado == EstadoReserva.PENDIENTE) {

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {

                            Button(
                                onClick = {

                                    val resultado =
                                        ReservasDemoRepository.cambiarEstado(
                                            reserva.id,
                                            EstadoReserva.APROBADA
                                        )

                                    if (resultado) {
                                        idReservaConError = null
                                    } else {
                                        idReservaConError = reserva.id
                                    }
                                }
                            ) {
                                Text("Aprobar")
                            }

                            Button(
                                onClick = {

                                    ReservasDemoRepository.cambiarEstado(
                                        reserva.id,
                                        EstadoReserva.RECHAZADA
                                    )

                                    idReservaConError = null
                                }
                            ) {
                                Text("Rechazar")
                            }
                        }

                        if (idReservaConError == reserva.id) {
                            Text(
                                text = "No se puede aprobar: existe otra reserva aprobada para ese espacio y horario.",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}
