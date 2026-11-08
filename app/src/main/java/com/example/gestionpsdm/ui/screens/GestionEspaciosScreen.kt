
package com.example.gestionpsdm.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

import com.example.gestionpsdm.R
import com.example.gestionpsdm.model.Espacio

@Composable
fun GestionEspaciosScreen(
    espacios: List<Espacio>,
    onCambiarEstado: (Int, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),

        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item {
            Text(
                text = "Administración de espacios",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Text(
                text = "Habilita o deshabilita los espacios comunitarios para permitir nuevas solicitudes de reserva.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (espacios.isEmpty()) {
            item {
                Text(
                    text = "No hay espacios registrados.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        items(
            items = espacios,
            key = { espacio -> espacio.id }
        ) { espacio ->

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

                    // Fotografía del espacio
                    Image(
                        painter = painterResource(imagenEspacio),

                        contentDescription =
                            "Fotografía del espacio ${espacio.nombre}",

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

                    Text(
                        text = "Capacidad máxima: ${espacio.capacidadMaxima} personas",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Estado actual
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color =
                            MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {

                        Text(
                            text = if (espacio.habilitado) {
                                "✓ Habilitado para reservas"
                            } else {
                                "No habilitado para reservas"
                            },

                            modifier = Modifier.padding(
                                horizontal = 14.dp,
                                vertical = 10.dp
                            ),

                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    HorizontalDivider()

                    // Control para permitir solicitudes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {

                            Text(
                                text = "Permitir solicitudes",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                text = if (espacio.habilitado) {
                                    "Los residentes pueden solicitar este espacio."
                                } else {
                                    "Los residentes no pueden solicitar este espacio."
                                },

                                style = MaterialTheme.typography.bodyMedium,
                                color =
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = espacio.habilitado,

                            onCheckedChange = { nuevoEstado ->
                                onCambiarEstado(
                                    espacio.id,
                                    nuevoEstado
                                )
                            },

                            modifier = Modifier.semantics {
                                contentDescription =
                                    "Permitir solicitudes para ${espacio.nombre}"
                            }
                        )
                    }
                }
            }
        }
    }
}
