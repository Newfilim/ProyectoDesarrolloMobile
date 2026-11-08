
package com.example.gestionpsdm.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

import com.example.gestionpsdm.model.EstadoPago
import com.example.gestionpsdm.model.EstadoReserva
import com.example.gestionpsdm.repository.EspaciosDemoRepository
import com.example.gestionpsdm.repository.ReservasDemoRepository
import com.example.gestionpsdm.utils.crearArchivoComprobante
import com.example.gestionpsdm.utils.crearUriComprobante
import com.example.gestionpsdm.viewmodel.ComprobantesViewModel

import java.io.File

@Composable
fun MisReservasScreen(
    comprobantesViewModel: ComprobantesViewModel,
    idUsuario: Int,
    modifier: Modifier = Modifier
) {

    // Mostrar solo las reservas del residente autenticado.
    val reservas = ReservasDemoRepository.reservas.filter {
        it.idUsuario == idUsuario
    }

    val comprobantesPorReserva =
        comprobantesViewModel.comprobantesPorReserva

    var idReservaSeleccionada by remember {
        mutableStateOf<Int?>(null)
    }

    // Seleccionar imagen desde la galería.
    val selectorImagen = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { resultado ->

        val idReserva = idReservaSeleccionada

        if (resultado != null && idReserva != null) {

            comprobantesViewModel.guardarComprobante(
                idReserva,
                resultado
            )
        }

        idReservaSeleccionada = null
    }

    val context = LocalContext.current

    var uriPendiente by remember {
        mutableStateOf<Uri?>(null)
    }

    var archivoPendiente by remember {
        mutableStateOf<File?>(null)
    }

    // Tomar fotografía utilizando la cámara.
    val camaraCompleta = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { exito ->

        val idReserva = idReservaSeleccionada
        val imagen = uriPendiente

        if (exito && idReserva != null && imagen != null) {

            comprobantesViewModel.guardarComprobante(
                idReserva,
                imagen
            )

        } else if (!exito) {

            archivoPendiente?.delete()
        }

        uriPendiente = null
        archivoPendiente = null
        idReservaSeleccionada = null
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),

        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item {
            Text(
                text = "Mis reservas",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Text(
                text = "Consulta tus solicitudes, revisa si fueron aprobadas y adjunta tus comprobantes de pago.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (reservas.isEmpty()) {

            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Text(
                        text = "Todavía no tienes reservas registradas. Puedes solicitar una desde Espacios comunitarios.",
                        modifier = Modifier.padding(20.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        items(
            items = reservas,
            key = { reserva -> reserva.id }
        ) { reserva ->

            val espacio = EspaciosDemoRepository.espacios.find {
                it.id == reserva.idEspacio
            }

            val nombreEspacio = espacio?.nombre
                ?: "Espacio no encontrado"

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

                    // NOMBRE DEL ESPACIO

                    Text(
                        text = nombreEspacio,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Reserva N.º ${reserva.id}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // ESTADO DE LA RESERVA

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {

                        Text(
                            text = when (reserva.estado) {

                                EstadoReserva.PENDIENTE ->
                                    "Solicitud pendiente"

                                EstadoReserva.APROBADA ->
                                    "✓ Reserva aprobada"

                                EstadoReserva.RECHAZADA ->
                                    "Reserva rechazada"

                                EstadoReserva.CANCELADA ->
                                    "Reserva cancelada"
                            },

                            modifier = Modifier.padding(
                                horizontal = 14.dp,
                                vertical = 10.dp
                            ),

                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // FECHA Y HORARIO

                    Text(
                        text = "Fecha de la reserva",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = reserva.fecha,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Text(
                        text = "Horario",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "${reserva.horaInicio} - ${reserva.horaFin}",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    HorizontalDivider()

                    // EXPLICACIÓN SEGÚN ESTADO DE RESERVA

                    if (reserva.estado == EstadoReserva.PENDIENTE) {

                        Text(
                            text = "Tu solicitud está esperando la revisión de Administración. Todavía no debes adjuntar un comprobante.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (reserva.estado == EstadoReserva.RECHAZADA) {

                        Text(
                            text = "Esta solicitud fue rechazada. Puedes revisar otros espacios o realizar una nueva solicitud.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (reserva.estado == EstadoReserva.CANCELADA) {

                        Text(
                            text = "Esta reserva fue cancelada.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // COMPROBANTES PARA RESERVAS APROBADAS

                    if (reserva.estado == EstadoReserva.APROBADA) {

                        val estadoPago =
                            comprobantesViewModel.estadosPorReserva[reserva.id]
                                ?: EstadoPago.PENDIENTE

                        Text(
                            text = "Estado del pago",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Crossfade(
                            targetState = estadoPago,
                            label = "animacionEstadoPagoResidente"
                        ) { estadoActual ->

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color =
                                    MaterialTheme.colorScheme.surfaceContainerHigh
                            ) {

                                Text(
                                    text = when (estadoActual) {

                                        EstadoPago.PENDIENTE ->
                                            "Pendiente de comprobante"

                                        EstadoPago.EN_REVISION ->
                                            "Comprobante en revisión"

                                        EstadoPago.CONFIRMADO ->
                                            "✓ Pago confirmado"

                                        EstadoPago.RECHAZADO ->
                                            "Comprobante rechazado"
                                    },

                                    modifier = Modifier.padding(14.dp),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // INSTRUCCIONES DEL PAGO

                        val explicacionPago = when (estadoPago) {

                            EstadoPago.PENDIENTE ->
                                "Debes enviar una fotografía del comprobante para que Tesorería revise tu pago."

                            EstadoPago.EN_REVISION ->
                                "Tu comprobante fue enviado. Tesorería debe revisarlo antes de confirmar el pago."

                            EstadoPago.CONFIRMADO ->
                                "Tesorería confirmó el pago de esta reserva."

                            EstadoPago.RECHAZADO ->
                                "Tu comprobante fue rechazado. Puedes enviar una nueva fotografía para que Tesorería la revise."
                        }

                        Text(
                            text = explicacionPago,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // BOTONES DE CÁMARA Y GALERÍA

                        AnimatedVisibility(
                            visible =
                                estadoPago == EstadoPago.PENDIENTE ||
                                        estadoPago == EstadoPago.RECHAZADO,

                            label = "animacionBotonesComprobante"
                        ) {

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement =
                                    Arrangement.spacedBy(12.dp)
                            ) {

                                Text(
                                    text = "¿Cómo deseas enviar el comprobante?",
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Text(
                                    text = "Selecciona una imagen guardada en tu teléfono o toma una fotografía nueva.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color =
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Button(
                                    onClick = {

                                        idReservaSeleccionada = reserva.id
                                        selectorImagen.launch("image/*")
                                    },

                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 56.dp),

                                    shape = RoundedCornerShape(14.dp)
                                ) {

                                    Text(
                                        text = "Elegir desde galería",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }

                                OutlinedButton(
                                    onClick = {

                                        idReservaSeleccionada = reserva.id

                                        val archivo =
                                            crearArchivoComprobante(context)

                                        archivoPendiente = archivo

                                        val uri = crearUriComprobante(
                                            context,
                                            archivo
                                        )

                                        uriPendiente = uri

                                        camaraCompleta.launch(uri)
                                    },

                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 56.dp),

                                    shape = RoundedCornerShape(14.dp)
                                ) {

                                    Text(
                                        text = "Tomar fotografía con cámara",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }

                        // COMPROBANTE ADJUNTO

                        val imagenReserva =
                            comprobantesPorReserva[reserva.id]

                        if (imagenReserva != null) {

                            Text(
                                text = "Comprobante adjuntado",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color =
                                    MaterialTheme.colorScheme.surfaceContainerHigh
                            ) {

                                AsyncImage(
                                    model = imagenReserva,

                                    contentDescription =
                                        "Comprobante adjuntado para la reserva ${reserva.id}",

                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(300.dp)
                                        .padding(8.dp),

                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}



