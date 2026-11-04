
package com.example.gestionpsdm.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.gestionpsdm.model.Espacio
import com.example.gestionpsdm.model.EstadoPago
import com.example.gestionpsdm.repository.ReservasDemoRepository
import com.example.gestionpsdm.viewmodel.ComprobantesViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RevisarPagosScreen(
    comprobantesViewModel: ComprobantesViewModel,
    espacios: List<Espacio>,
    modifier: Modifier = Modifier
) {
    val comprobantes = comprobantesViewModel.comprobantesPorReserva

    val listaComprobantes = comprobantes.toList()

    val alcance = rememberCoroutineScope()

    var idPagoProcesando by remember {
        mutableStateOf<Int?>(null)
    }

    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item {
            Text(
                text = "Revisión de pagos",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        item {
            Text(
                text = "Aquí puedes revisar los comprobantes enviados por los residentes."
            )
        }

        if (listaComprobantes.isEmpty()) {

            item {
                Text("Todavía no hay comprobantes para revisar.")
            }

        } else {

            items(
                items = listaComprobantes,
                key = { elemento -> elemento.first }
            ) { elemento ->

                val idReserva = elemento.first
                val imagen = elemento.second

                val reserva = ReservasDemoRepository.reservas.find {
                    it.id == idReserva
                }

                val estadoPago =
                    comprobantesViewModel.estadosPorReserva[idReserva]
                        ?: EstadoPago.PENDIENTE

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        Text(
                            text = "Reserva N.º $idReserva",
                            style = MaterialTheme.typography.titleMedium
                        )

                        if (reserva != null) {

                            val espacio = espacios.find {
                                it.id == reserva.idEspacio
                            }

                            if (espacio != null) {
                                Text(
                                    "Espacio solicitado: ${espacio.nombre}"
                                )
                            }

                            Text("Fecha: ${reserva.fecha}")

                            Text(
                                "Horario: ${reserva.horaInicio} - ${reserva.horaFin}"
                            )
                        }

                        Text("Estado del pago: ${estadoPago.name}")

                        AsyncImage(
                            model = imagen,
                            contentDescription = "Comprobante de la reserva $idReserva",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentScale = ContentScale.Fit
                        )

                        if (estadoPago == EstadoPago.EN_REVISION) {

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {

                                Button(
                                    onClick = {

                                        idPagoProcesando = idReserva

                                        alcance.launch {

                                            delay(800)

                                            comprobantesViewModel.actualizarEstadoPago(
                                                idReserva,
                                                EstadoPago.CONFIRMADO
                                            )

                                            idPagoProcesando = null
                                        }
                                    },
                                    enabled = idPagoProcesando == null
                                ) {

                                    if (idPagoProcesando == idReserva) {

                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )

                                    } else {

                                        Text("Confirmar")
                                    }
                                }

                                Button(
                                    onClick = {

                                        comprobantesViewModel.actualizarEstadoPago(
                                            idReserva,
                                            EstadoPago.RECHAZADO
                                        )
                                    },
                                    enabled = idPagoProcesando == null
                                ) {
                                    Text("Rechazar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
