
package com.example.gestionpsdm.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

import com.example.gestionpsdm.R
import com.example.gestionpsdm.model.Espacio
import com.example.gestionpsdm.viewmodel.PreferenciasViewModel

@Composable
fun EspaciosScreen(
    onSolicitarReserva: (Int) -> Unit,
    espacios: List<Espacio>,
    preferenciasViewModel: PreferenciasViewModel,
    modifier: Modifier = Modifier
) {

    val mostrarSoloHabilitados by
    preferenciasViewModel.soloHabilitados.collectAsState()

    val espaciosVisibles = if (mostrarSoloHabilitados) {
        espacios.filter { espacio ->
            espacio.habilitado
        }
    } else {
        espacios
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item {
            Text(
                text = "Espacios comunitarios",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Text(
                text = "Elige un espacio para conocer su capacidad y solicitar una reserva.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme.surfaceContainer
                )
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "Mostrar solo disponibles",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )

                    Switch(
                        checked = mostrarSoloHabilitados,
                        onCheckedChange = { nuevoValor ->
                            preferenciasViewModel
                                .cambiarSoloHabilitados(nuevoValor)
                        }
                    )
                }
            }
        }

        if (espaciosVisibles.isEmpty()) {
            item {
                Text(
                    text = "No hay espacios disponibles en este momento.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        items(
            items = espaciosVisibles,
            key = { espacio -> espacio.id }
        ) { espacio ->

            TarjetaEspacio(
                onSolicitarReserva = onSolicitarReserva,
                espacio = espacio
            )
        }
    }
}

@Composable
fun TarjetaEspacio(
    onSolicitarReserva: (Int) -> Unit,
    espacio: Espacio
) {

    val imagenEspacio = when (espacio.nombre) {
        "Quincho" -> R.drawable.quincho
        "Piscina" -> R.drawable.piscina
        "Sala multiuso" -> R.drawable.sala_multiuso
        else -> R.drawable.quincho
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surfaceContainer
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            Image(
                painter = painterResource(imagenEspacio),
                contentDescription = "Fotografía del espacio ${espacio.nombre}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Crop
            )

            Text(
                text = espacio.nombre,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (espacio.habilitado) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                }
            ) {
                Text(
                    text = if (espacio.habilitado) {
                        "✓ Disponible para reservar"
                    } else {
                        "No disponible actualmente"
                    },
                    modifier = Modifier.padding(
                        horizontal = 14.dp,
                        vertical = 10.dp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text(
                text = "Capacidad máxima: ${espacio.capacidadMaxima} personas",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = if (espacio.habilitado) {
                    "Presiona el botón para elegir la fecha y el horario."
                } else {
                    "Este espacio está deshabilitado. Por ahora no admite nuevas reservas."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = {
                    onSolicitarReserva(espacio.id)
                },
                enabled = espacio.habilitado,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (espacio.habilitado) {
                        "Solicitar reserva  →"
                    } else {
                        "Reservas no disponibles"
                    },
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
