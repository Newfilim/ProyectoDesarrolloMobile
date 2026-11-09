
package com.example.gestionpsdm.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.example.gestionpsdm.viewmodel.FinanzasViewModel

import java.text.NumberFormat
import java.util.Locale

import com.example.gestionpsdm.ui.components.GraficoGastosCategoria
import com.example.gestionpsdm.ui.components.GraficoIngresos
import com.example.gestionpsdm.ui.components.GraficoBalanceCircular

@Composable
fun InformeFinancieroScreen(
    viewModel: FinanzasViewModel,
    modifier: Modifier = Modifier
) {

    val resumen by viewModel.resumen.collectAsState()
    val gastosPorCategoria by viewModel.gastosPorCategoria.collectAsState()
    val ingresosRegistrados by viewModel.ingresosRegistrados.collectAsState()

    val totalIngresos = resumen.ingresos
    val totalGastos = resumen.gastos
    val balance = resumen.balance

    val formatoPesos = NumberFormat.getIntegerInstance(
        Locale("es", "CL")
    )

    val estadoBalance = when {
        balance > 0 -> "Balance positivo"
        balance < 0 -> "Balance negativo"
        else -> "Balance en cero"
    }

    val explicacionBalance = when {
        balance > 0 ->
            "Los ingresos registrados superan los gastos."

        balance < 0 ->
            "Los gastos registrados superan los ingresos."

        else ->
            "Los ingresos y gastos registrados son iguales."
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),

        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Informe financiero",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Consulta el resumen de los ingresos y gastos registrados por la comunidad.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (resumen.cargando) {

            Text(
                text = "Cargando información financiera...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

        } else {

            IndicadorFinanciero(
                titulo = "Ingresos registrados",
                monto = "\$${formatoPesos.format(totalIngresos)}",
                descripcion = "Dinero registrado como ingreso de la comunidad."
            )

            IndicadorFinanciero(
                titulo = "Gastos registrados",
                monto = "\$${formatoPesos.format(totalGastos)}",
                descripcion = "Dinero utilizado en gastos de la comunidad."
            )

            // Balance destacado
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),

                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme.surfaceContainerHigh
                )
            )
            {

                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Text(
                        text = "Balance neto",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "\$${formatoPesos.format(balance)}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = estadoBalance,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = explicacionBalance,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Cálculo: ingresos registrados menos gastos registrados.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            GraficoGastosCategoria(
                gastos = gastosPorCategoria
            )

            GraficoIngresos(
                ingresos = ingresosRegistrados,
                totalIngresos = totalIngresos
            )

            GraficoBalanceCircular(
                ingresos = totalIngresos,
                gastos = totalGastos
            )


        }
    }
}

@Composable
private fun IndicadorFinanciero(
    titulo: String,
    monto: String,
    descripcion: String
) {

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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = monto,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = descripcion,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
