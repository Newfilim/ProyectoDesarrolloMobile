
package com.example.gestionpsdm.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

import com.example.gestionpsdm.model.Espacio
import com.example.gestionpsdm.model.EstadoPago
import com.example.gestionpsdm.model.Reserva
import com.example.gestionpsdm.repository.UsuariosDemoRepository
import com.example.gestionpsdm.viewmodel.ComprobantesViewModel


@Composable
fun RevisarPagosScreen(
    comprobantesViewModel: ComprobantesViewModel,
    espacios: List<Espacio>,
    reservas: List<Reserva>,
    modifier: Modifier = Modifier
) {

    val comprobantes = comprobantesViewModel.comprobantesPorReserva
    val listaComprobantes = comprobantes.toList()

    var idPagoProcesando by remember {
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
                text = "Revisión de pagos",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Text(
                text = "Revisa los datos y la imagen de cada comprobante antes de confirmar o rechazar un pago.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (listaComprobantes.isEmpty()) {

            item {
                Text(
                    text = "Todavía no hay comprobantes para revisar.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

        } else {

            items(
                items = listaComprobantes,
                key = { elemento -> elemento.first }
            ) { elemento ->

                val idReserva = elemento.first
                val imagen = elemento.second

                val reserva = reservas.find {
                    it.id == idReserva
                }

                val residente = UsuariosDemoRepository.obtenerUsuarios().find {
                    it.id == reserva?.idUsuario
                }

                val espacio = espacios.find {
                    it.id == reserva?.idEspacio
                }

                val estadoPago =
                    comprobantesViewModel.estadosPorReserva[idReserva]
                        ?: EstadoPago.PENDIENTE

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

                        Text(
                            text = "Reserva N.º $idReserva",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color =
                                MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {

                            Text(
                                text = when (estadoPago) {

                                    EstadoPago.PENDIENTE ->
                                        "Pago pendiente"

                                    EstadoPago.EN_REVISION ->
                                        "Comprobante en revisión"

                                    EstadoPago.CONFIRMADO ->
                                        "✓ Pago confirmado"

                                    EstadoPago.RECHAZADO ->
                                        "Comprobante rechazado"
                                },
                                modifier = Modifier.padding(
                                    horizontal = 14.dp,
                                    vertical = 10.dp
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "Residente",
                            style = MaterialTheme.typography.bodyMedium,
                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = residente?.nombre
                                ?: "Residente no encontrado",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Text(
                            text = "Espacio solicitado",
                            style = MaterialTheme.typography.bodyMedium,
                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = espacio?.nombre
                                ?: "Espacio no encontrado",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        if (reserva != null) {

                            Text(
                                text = "Fecha: ${reserva.fecha}",
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Text(
                                text = "Horario: ${reserva.horaInicio} - ${reserva.horaFin}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        Text(
                            text = "Comprobante adjunto",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = "Comprueba que la imagen corresponda al pago antes de tomar una decisión.",
                            style = MaterialTheme.typography.bodyMedium,
                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color =
                                MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {

                            AsyncImage(
                                model = imagen,
                                contentDescription =
                                    "Comprobante de la reserva $idReserva",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                                    .padding(8.dp),
                                contentScale = ContentScale.Fit
                            )
                        }

                        if (estadoPago == EstadoPago.EN_REVISION) {

                            Text(
                                text = "Selecciona una acción después de revisar el comprobante:",
                                style = MaterialTheme.typography.bodyMedium,
                                color =
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Button(
                                onClick = {

                                    idPagoProcesando = idReserva

                                    comprobantesViewModel.actualizarEstadoPago(
                                        idReserva,
                                        EstadoPago.CONFIRMADO
                                    ) { resultado ->
                                        idPagoProcesando = null
                                    }
                                },

                                enabled = idPagoProcesando == null,

                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 56.dp),

                                shape = RoundedCornerShape(14.dp)
                            ) {

                                if (idPagoProcesando == idReserva) {

                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color =
                                            MaterialTheme.colorScheme.onPrimary
                                    )

                                } else {

                                    Text(
                                        text = "✓ Confirmar pago",
                                        style =
                                            MaterialTheme.typography.labelLarge
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = {

                                    comprobantesViewModel
                                        .actualizarEstadoPago(
                                            idReserva,
                                            EstadoPago.RECHAZADO
                                        )
                                },

                                enabled = idPagoProcesando == null,

                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 56.dp),

                                shape = RoundedCornerShape(14.dp)
                            ) {

                                Text(
                                    text = "✕ Rechazar comprobante",
                                    style =
                                        MaterialTheme.typography.labelLarge
                                )
                            }
                        }

                        if (estadoPago == EstadoPago.CONFIRMADO) {

                            Text(
                                text = "El pago de esta reserva fue confirmado.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (estadoPago == EstadoPago.RECHAZADO) {

                            Text(
                                text = "Este comprobante fue rechazado.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

