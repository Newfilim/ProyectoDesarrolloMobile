
package com.example.gestionpsdm.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RevisarPagosScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Revisión de pagos",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Aquí se revisarán los comprobantes enviados por los residentes."
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Comprobante de ejemplo",
                    style = MaterialTheme.typography.titleMedium
                )

                Text("Reserva N.º 1 (dato ficticio)")
                Text("Estado: Pendiente de revisión")
                Text("Archivo: comprobante_ejemplo.jpg")

                Button(
                    onClick = {},
                    enabled = false
                ) {
                    Text("Ver comprobante")
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {},
                        enabled = false
                    ) {
                        Text("Confirmar")
                    }

                    Button(
                        onClick = {},
                        enabled = false
                    ) {
                        Text("Rechazar")
                    }
                }
            }
        }
    }
}
