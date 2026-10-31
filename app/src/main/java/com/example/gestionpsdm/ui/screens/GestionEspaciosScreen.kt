
package com.example.gestionpsdm.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gestionpsdm.model.Espacio

@Composable
fun GestionEspaciosScreen(
    espacios: List<Espacio>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Administración de espacios",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        item {
            Button(
                onClick = {},
                enabled = false
            ) {
                Text("Agregar espacio")
            }
        }

        items(espacios) { espacio ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = espacio.nombre,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "Capacidad: ${espacio.capacidadMaxima} personas"
                    )

                    Text(
                        text = if (espacio.habilitado) {
                            "Estado: Disponible"
                        } else {
                            "Estado: Deshabilitado"
                        }
                    )

                    Button(
                        onClick = {},
                        enabled = false
                    ) {
                        Text("Editar espacio")
                    }
                }
            }
        }
    }
}
