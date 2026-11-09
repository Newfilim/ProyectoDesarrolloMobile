
package com.example.gestionpsdm.ui.screens

import android.widget.Toast
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.gestionpsdm.viewmodel.ReservaViewModel
import com.example.gestionpsdm.ui.components.MensajeError

@Composable
fun SolicitarReservaScreen(
    viewModel: ReservaViewModel,
    modifier: Modifier = Modifier,
    idUsuario: Int,
    idEspacio: Int
) {

    val estado by viewModel.estado.collectAsState()
    val errores by viewModel.errores.collectAsState()
    val guardando by viewModel.guardando.collectAsState()
    val context = LocalContext.current

    var mostrarConfirmacion by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Solicitar reserva",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Completa la fecha y el horario en que deseas utilizar el espacio. Luego envía tu solicitud para que Administración la revise.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Text(
                    text = "1. Fecha de la reserva",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Escribe la fecha con el formato DD/MM/AAAA. No puedes seleccionar una fecha pasada.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = estado.fecha,
                    onValueChange = { nuevaFecha ->
                        viewModel.actualizarFecha(nuevaFecha)
                        mostrarConfirmacion = false
                    },
                    label = { Text("Fecha (DD/MM/AAAA)") },
                    placeholder = { Text("Ejemplo: 20/11/2026") },
                    isError = errores.fecha != null,
                    supportingText = {
                        MensajeError(errores.fecha)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Text(
                    text = "2. Horario de la reserva",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Ingresa la hora de inicio y término utilizando el formato de 24 horas (HH:mm).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Domingo a jueves: de 08:00 a 22:00. Viernes y sábado: desde las 08:00 y hasta las 02:00 del día siguiente.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = estado.horaInicio,
                    onValueChange = { nuevaHora ->
                        viewModel.actualizarHoraInicio(nuevaHora)
                        mostrarConfirmacion = false
                    },
                    label = { Text("Hora de inicio") },
                    placeholder = { Text("Ejemplo: 15:00") },
                    isError = errores.horaInicio != null,
                    supportingText = {
                        MensajeError(errores.horaInicio)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = estado.horaFin,
                    onValueChange = { nuevaHora ->
                        viewModel.actualizarHoraFin(nuevaHora)
                        mostrarConfirmacion = false
                    },
                    label = { Text("Hora de término") },
                    placeholder = { Text("Ejemplo: 17:00") },
                    isError = errores.horaFin != null,
                    supportingText = {
                        MensajeError(errores.horaFin)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Button(
            onClick = {

                mostrarConfirmacion = false

                viewModel.registrarReserva(
                    idUsuario = idUsuario,
                    idEspacio = idEspacio
                ) { correcto, mensaje ->

                    if (correcto) {
                        mostrarConfirmacion = true
                    }

                    Toast.makeText(
                        context,
                        mensaje,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            enabled = !guardando,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            shape = RoundedCornerShape(14.dp)
        ) {

            Text(
                text = if (guardando) {
                    "Guardando solicitud..."
                } else {
                    "Enviar solicitud de reserva  →"
                },
                style = MaterialTheme.typography.labelLarge
            )
        }


        if (mostrarConfirmacion) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme.primaryContainer
                )
            ) {

                Text(
                    text = "✓ Solicitud registrada correctamente. Ahora debes esperar que Administración la apruebe. Puedes consultar su estado en Mis reservas.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
