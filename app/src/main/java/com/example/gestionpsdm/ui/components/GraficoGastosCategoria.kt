
package com.example.gestionpsdm.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.gestionpsdm.data.local.GastoPorCategoria
import com.example.gestionpsdm.model.CategoriaGasto
import java.text.NumberFormat
import java.util.Locale

@Composable
fun GraficoGastosCategoria(
    gastos: List<GastoPorCategoria>,
    modifier: Modifier = Modifier
) {

    val formatoPesos = NumberFormat.getIntegerInstance(
        Locale("es", "CL")
    )

    val montoMayor = gastos.maxOfOrNull { it.total } ?: 0L
    val totalGastos = gastos.sumOf { it.total }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Gastos por categoría",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "Distribución de los gastos comunitarios registrados.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (gastos.isEmpty()) {

                Text(
                    text = "Todavía no hay gastos registrados.",
                    style = MaterialTheme.typography.bodyLarge
                )

            } else {

                for (gasto in gastos) {

                    val categoria = gasto.categoria

                    val nombre = categoria
                        .replace('_', ' ')
                        .lowercase()
                        .replaceFirstChar { it.uppercase() }

                    val colorBarra = obtenerColorCategoria(categoria)

                    val proporcion = if (montoMayor > 0L) {
                        (gasto.total.toFloat() / montoMayor.toFloat())
                            .coerceIn(0f, 1f)
                    } else {
                        0f
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {

                            Text(
                                text = nombre,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Text(
                                text = "\$${formatoPesos.format(gasto.total)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(14.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    MaterialTheme.colorScheme.surfaceContainerHigh
                                )
                        ) {

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(proporcion)
                                    .height(14.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colorBarra)
                            )
                        }
                    }
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text(
                        text = "Total de gastos",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "\$${formatoPesos.format(totalGastos)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

private fun obtenerColorCategoria(
    categoria: String
): Color {

    return when (categoria) {

        CategoriaGasto.SERVICIOS_BASICOS.name ->
            Color(0xFFF3B75B)

        CategoriaGasto.LIMPIEZA.name ->
            Color(0xFF48C9B0)

        CategoriaGasto.INSUMOS.name ->
            Color(0xFF8EADFF)

        CategoriaGasto.MANTENIMIENTO.name ->
            Color(0xFFF08B88)

        CategoriaGasto.MEJORAS.name ->
            Color(0xFFD98BEE)

        CategoriaGasto.OTROS.name ->
            Color(0xFFA7AFC5)

        else ->
            Color(0xFFA7AFC5)
    }
}
