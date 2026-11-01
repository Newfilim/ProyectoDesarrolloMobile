
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
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.material3.Button
import androidx.compose.ui.platform.LocalContext
import com.example.gestionpsdm.utils.crearArchivoComprobante
import com.example.gestionpsdm.utils.crearUriComprobante
import java.io.File
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.gestionpsdm.viewmodel.ComprobantesViewModel
import com.example.gestionpsdm.repository.ReservasDemoRepository
import com.example.gestionpsdm.model.EstadoPago
import androidx.compose.animation.AnimatedVisibility

@Composable
fun MisReservasScreen(
    comprobantesViewModel: ComprobantesViewModel,
    modifier: Modifier = Modifier

) {
    val reservas = ReservasDemoRepository.reservas
    val comprobantesPorReserva = comprobantesViewModel.comprobantesPorReserva

    var idReservaSeleccionada by remember {
        mutableStateOf<Int?>(null)
    }


    val selectorImagen = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { resultado ->
        val idReserva = idReservaSeleccionada
        if (resultado != null && idReserva != null) {
            comprobantesViewModel.guardarComprobante(idReserva, resultado)
        }
    }


    val context = LocalContext.current

    var uriPendiente by remember {
        mutableStateOf<Uri?>(null)
    }

    var archivoPendiente by remember {
        mutableStateOf<File?>(null)
    }
    val camaraCompleta = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { exito ->

        val idReserva = idReservaSeleccionada
        val imagen = uriPendiente

        if (exito && idReserva != null && imagen != null) {
            comprobantesViewModel.guardarComprobante(idReserva, imagen)
        } else if (!exito) {
            archivoPendiente?.delete()
        }

        uriPendiente = null
        archivoPendiente = null
    }







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
                    if (reserva.estado == EstadoReserva.APROBADA) {
                        val estadoPago = comprobantesViewModel.estadosPorReserva[reserva.id]
                            ?: EstadoPago.PENDIENTE

                        Crossfade(
                            targetState = estadoPago,
                            label = "animacionEstadoPagoResidente"
                        ) { estadoActual ->
                            Text("Estado del pago: ${estadoActual.name}")
                        }
                        AnimatedVisibility(
                    estadoPago == EstadoPago.PENDIENTE ||
                            estadoPago == EstadoPago.RECHAZADO,
                            label = "animacionBotonesComprobante") {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ){
                                Button(
                                    onClick = {
                                        idReservaSeleccionada = reserva.id
                                        selectorImagen.launch("image/*")
                                    }
                                ) {
                                    Text("Seleccionar desde galería")
                                }
                                Button(
                                    onClick = {
                                        idReservaSeleccionada = reserva.id
                                        val archivo = crearArchivoComprobante(context)
                                        archivoPendiente = archivo
                                        val uri = crearUriComprobante(context, archivo)
                                        uriPendiente = uri
                                        camaraCompleta.launch(uri)
                                    }
                                ) {
                                    Text("Tomar fotografía completa")
                                }
                            }


                        }

                        val imagenReserva = comprobantesPorReserva[reserva.id]

                        if (imagenReserva != null) {

                            Text("Comprobante adjuntado:")

                            AsyncImage(
                                model = imagenReserva,
                                contentDescription = "Comprobante de la reserva ${reserva.id}",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                    }
                }
            }
        }
    }


