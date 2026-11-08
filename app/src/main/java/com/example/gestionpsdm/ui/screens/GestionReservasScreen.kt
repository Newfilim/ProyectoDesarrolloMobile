
package com.example.gestionpsdm.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import com.example.gestionpsdm.repository.UsuariosDemoRepository
import com.example.gestionpsdm.repository.EspaciosDemoRepository

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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item {
            Text(
                text = "Gestión de reservas",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Text(
                text = "Revisa los datos de cada solicitud antes de aprobarla o rechazarla.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (reservas.isEmpty()) {
            item {
                Text(
                    text = "Todavía no hay solicitudes de reserva.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        items(
            items = reservas,
            key = { reserva -> reserva.id }
        ) { reserva ->
            val usuario = UsuariosDemoRepository.obtenerUsuarios()
                .find { it.id == reserva.idUsuario }

            val espacio = EspaciosDemoRepository.espacios
                .find { it.id == reserva.idEspacio }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme.surfaceContainer
                )
            ) {

                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    // Encabezado de la reserva
                    Text(
                        text = "Reserva N.º ${reserva.id}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Estado de la reserva
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {

                        Text(
                            text = when (reserva.estado) {
                                EstadoReserva.PENDIENTE -> "Pendiente"
                                EstadoReserva.APROBADA -> "✓ Aprobada"
                                EstadoReserva.RECHAZADA -> "Rechazada"
                                EstadoReserva.CANCELADA -> "Cancelada"
                            },
                            modifier = Modifier.padding(
                                horizontal = 14.dp,
                                vertical = 10.dp
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Información de la solicitud
                    Text(
                        text = "Residente: ${usuario?.nombre ?: "Usuario no encontrado"}",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Text(
                        text = "Espacio: ${espacio?.nombre ?: "Espacio no encontrado"}",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Text(
                        text = "Fecha: ${reserva.fecha}",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Text(
                        text = "Horario: ${reserva.horaInicio} - ${reserva.horaFin}",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    if (reserva.estado == EstadoReserva.PENDIENTE) {

                        Text(
                            text = "Selecciona qué deseas hacer con esta solicitud:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Aprobar solicitud
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
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {

                            Text(
                                text = "✓ Aprobar reserva",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        // Rechazar solicitud
                        OutlinedButton(
                            onClick = {

                                ReservasDemoRepository.cambiarEstado(
                                    reserva.id,
                                    EstadoReserva.RECHAZADA
                                )

                                idReservaConError = null
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {

                            Text(
                                text = "✕ Rechazar reserva",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        // Mensaje cuando existe un conflicto de horario
                        if (idReservaConError == reserva.id) {

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.errorContainer
                            ) {

                                Text(
                                    text = "No se puede aprobar esta solicitud porque existe otra reserva aprobada para el mismo espacio y horario.",
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
