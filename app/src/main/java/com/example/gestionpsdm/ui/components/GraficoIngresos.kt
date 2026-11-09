
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
import com.example.gestionpsdm.model.Ingreso
import java.text.NumberFormat
import java.util.Locale

@Composable
fun GraficoIngresos(
    ingresos: List<Ingreso>,
    totalIngresos: Long,
    modifier: Modifier = Modifier
) {

    val formatoPesos = NumberFormat.getIntegerInstance(
        Locale("es", "CL")
    )

    // El DAO entrega los ingresos por ID descendente.
    // Mostramos los últimos ocho registros.
    val ingresosVisibles = ingresos.take(8)

    val montoMayor = ingresosVisibles.maxOfOrNull {
        it.monto
    } ?: 0

    val colores = listOf(
        Color(0xFF71B7FF),
        Color(0xFFB69DF6),
        Color(0xFF56D3C4),
        Color(0xFFF0BB78),
        Color(0xFFF08B88),
        Color(0xFF8EADFF),
        Color(0xFFD98BEE),
        Color(0xFFA7AFC5)
    )

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
                text = "Ingresos por arriendo",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "Comparación de los arriendos registrados.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (ingresos.isEmpty()) {

                Text(
                    text = "Todavía no hay ingresos registrados.",
                    style = MaterialTheme.typography.bodyLarge
                )

            } else {

                for (indice in ingresosVisibles.indices) {

                    val ingreso = ingresosVisibles[indice]

                    val colorBarra = colores[
                        indice % colores.size
                    ]

                    val proporcion = if (montoMayor > 0) {
                        (
                                ingreso.monto.toFloat() /
                                        montoMayor.toFloat()
                                ).coerceIn(0f, 1f)
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

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {

                                Text(
                                    text = ingreso.descripcion,
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Text(
                                    text = ingreso.fecha,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Text(
                                text = "\$${formatoPesos.format(ingreso.monto)}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp)
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

                if (ingresos.size > 8) {

                    Text(
                        text = "Mostrando los últimos 8 de ${ingresos.size} ingresos registrados.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text(
                        text = "Total de ingresos",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "\$${formatoPesos.format(totalIngresos)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
