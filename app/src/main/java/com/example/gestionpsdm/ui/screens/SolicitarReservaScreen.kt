package com.example.gestionpsdm.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.gestionpsdm.utils.HorariosReserva
import com.example.gestionpsdm.viewmodel.ReservaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitarReservaScreen(
    viewModel: ReservaViewModel,
    modifier: Modifier = Modifier,
    idUsuario: Int,
    idEspacio: Int
) {
    val estado by viewModel.estado.collectAsState()
    val guardando by viewModel.guardando.collectAsState()
    val ahora by viewModel.ahora.collectAsState()
    val context = LocalContext.current

    var fecha by rememberSaveable(idEspacio) { mutableStateOf<String?>(null) }
    var mostrarCalendario by rememberSaveable { mutableStateOf(false) }
    var mostrarConfirmacion by remember { mutableStateOf(false) }

    LaunchedEffect(idEspacio, fecha) {
        viewModel.observarFecha(idEspacio, fecha)
    }

    if (mostrarCalendario) {
        // DatePicker representa fechas civiles como medianoche UTC.
        val formatoUtc = remember {
            SimpleDateFormat("dd/MM/yyyy", Locale.ROOT).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
        }
        val hoy = HorariosReserva.fecha(ahora)
        val hoyUtc = formatoUtc.parse(hoy)?.time ?: 0L
        val seleccionInicial = fecha?.let { formatoUtc.parse(it)?.time }
        val fechaMinima = remember(hoyUtc) {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis >= hoyUtc
                }

                override fun isSelectableYear(year: Int): Boolean {
                    return year >= hoy.takeLast(4).toInt()
                }
            }
        }
        val calendario = rememberDatePickerState(
            initialSelectedDateMillis = seleccionInicial,
            initialDisplayedMonthMillis = maxOf(hoyUtc, seleccionInicial ?: hoyUtc),
            selectableDates = fechaMinima
        )
        val fechaElegida = calendario.selectedDateMillis

        DatePickerDialog(
            onDismissRequest = { mostrarCalendario = false },
            confirmButton = {
                TextButton(
                    enabled = fechaElegida != null && fechaElegida >= hoyUtc,
                    onClick = {
                        if (fechaElegida != null) {
                            fecha = formatoUtc.format(Date(fechaElegida))
                            mostrarConfirmacion = false
                            mostrarCalendario = false
                        }
                    }
                ) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarCalendario = false }) { Text("Volver") }
            }
        ) {
            DatePicker(state = calendario, showModeToggle = false)
        }
    }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Solicitar reserva", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Selecciona la fecha y el horario para enviar tu solicitud a Administración.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Fecha de la reserva", style = MaterialTheme.typography.titleMedium)
                OutlinedButton(
                    enabled = !guardando,
                    onClick = { mostrarCalendario = true }
                ) {
                    Text(fecha ?: "Abrir calendario")
                }
            }
        }

        if (fecha != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Horario de la reserva", style = MaterialTheme.typography.titleMedium)
                    if (estado.cargando || estado.fecha != fecha) {
                        Text("Consultando disponibilidad…")
                    } else {
                        if (estado.bloques.none { it.disponible }) {
                            Text("No hay horarios disponibles para esta fecha.")
                        } else {
                            Text(
                                "Toca un bloque y, para extender la reserva, toca otro sin interrupciones entre ambos.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        for ((fila, bloques) in estado.bloques.chunked(2).withIndex()) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                for ((columna, bloque) in bloques.withIndex()) {
                                    val indice = fila * 2 + columna
                                    val seleccionado = estado.seleccion?.contains(indice) == true
                                    val inicio = HorariosReserva.hora(bloque.inicio)
                                    val fin = HorariosReserva.hora(bloque.fin)
                                    val fechaInicio = HorariosReserva.fecha(bloque.inicio)
                                    FilterChip(
                                        selected = seleccionado,
                                        enabled = bloque.disponible && !guardando,
                                        onClick = {
                                            mostrarConfirmacion = false
                                            if (!viewModel.seleccionarBloque(indice)) {
                                                Toast.makeText(
                                                    context,
                                                    "Selecciona un rango continuo de bloques disponibles.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        label = {
                                            Column(Modifier.padding(vertical = 6.dp)) {
                                                Text("$inicio–$fin")
                                                Text(
                                                    when {
                                                        seleccionado -> "Seleccionado"
                                                        bloque.disponible -> "Disponible"
                                                        else -> "No disponible"
                                                    },
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                                if (fechaInicio != fecha) {
                                                    Text(
                                                        "Día siguiente",
                                                        style = MaterialTheme.typography.bodySmall
                                                    )
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        val rango = estado.seleccion
                        if (estado.rangoDisponible && rango != null) {
                            val inicio = estado.bloques[rango.first].inicio
                            val fin = estado.bloques[rango.last].fin
                            val horas = rango.count()
                            val duracion = if (horas == 1) "1 hora" else "$horas horas"
                            Text(
                                "Inicio  ${HorariosReserva.fecha(inicio)} · ${HorariosReserva.hora(inicio)}\n" +
                                    "Término  ${HorariosReserva.fecha(fin)} · ${HorariosReserva.hora(fin)}\n" +
                                    "Duración  $duracion",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            TextButton(
                                enabled = !guardando,
                                onClick = { viewModel.limpiarSeleccion() }
                            ) { Text("Limpiar selección") }
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                mostrarConfirmacion = false
                viewModel.registrarReserva(idUsuario, idEspacio) { correcto, mensaje ->
                    mostrarConfirmacion = correcto
                    Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
                }
            },
            enabled = estado.fecha == fecha && estado.rangoDisponible && !guardando,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(if (guardando) "Guardando solicitud…" else "Enviar solicitud de reserva →")
        }

        if (mostrarConfirmacion) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Text(
                    "Solicitud registrada. Puedes seguir su aprobación en Mis reservas.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
