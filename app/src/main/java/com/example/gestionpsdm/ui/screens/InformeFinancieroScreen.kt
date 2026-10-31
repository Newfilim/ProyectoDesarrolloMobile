
package com.example.gestionpsdm.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun InformeFinancieroScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Informe financiero",
            style = MaterialTheme.typography.headlineMedium
        )

        Text("Resumen financiero de la comunidad.")

        IndicadorFinanciero(
            titulo = "Ingresos confirmados",
            descripcion = "Sin ingresos registrados todavía."
        )

        IndicadorFinanciero(
            titulo = "Gastos",
            descripcion = "Sin gastos registrados todavía."
        )

        IndicadorFinanciero(
            titulo = "Balance",
            descripcion = "Se calculará al registrar ingresos y gastos."
        )
    }
}

@Composable
private fun IndicadorFinanciero(
    titulo: String,
    descripcion: String
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium
            )

            Text(descripcion)
        }
    }
}
