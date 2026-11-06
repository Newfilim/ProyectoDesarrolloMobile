
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
import com.example.gestionpsdm.repository.GastosRepository
import com.example.gestionpsdm.repository.IngresosRepository

@Composable
fun InformeFinancieroScreen(
    modifier: Modifier = Modifier
) {

    val totalIngresos = IngresosRepository.obtenerTotalIngresos()
    val totalGastos = GastosRepository.obtenerTotalGastos()
    val balance = totalIngresos - totalGastos

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
            titulo = "Ingresos registrados",
            descripcion = "$$totalIngresos CLP"
        )

        IndicadorFinanciero(
            titulo = "Gastos registrados",
            descripcion = "$$totalGastos CLP"
        )

        IndicadorFinanciero(
            titulo = "Balance",
            descripcion = "$$balance CLP"
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

