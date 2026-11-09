
package com.example.gestionpsdm.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.Locale

@Composable
fun GraficoBalanceCircular(
    ingresos: Long,
    gastos: Long,
    modifier: Modifier = Modifier
) {

    val formatoPesos = NumberFormat.getIntegerInstance(
        Locale("es", "CL")
    )

    val balance = ingresos - gastos

    val colorIngresos = Color(0xFF48C9B0)
    val colorGastos = Color(0xFFF08B88)
    val colorFondo = MaterialTheme.colorScheme.surfaceContainerHigh

    // Usamos Double para evitar problemas al sumar montos grandes.
    val totalMovimientos = ingresos.toDouble() + gastos.toDouble()

    val porcentajeIngresos = if (totalMovimientos > 0.0) {
        ingresos.toDouble() / totalMovimientos
    } else {
        0.0
    }

    val porcentajeGastos = if (totalMovimientos > 0.0) {
        gastos.toDouble() / totalMovimientos
    } else {
        0.0
    }

    val anguloIngresos = (porcentajeIngresos * 360.0).toFloat()
    val anguloGastos = (porcentajeGastos * 360.0).toFloat()

    val estadoBalance = when {
        balance > 0 -> "Balance positivo"
        balance < 0 -> "Balance negativo"
        else -> "Balance en cero"
    }

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
                text = "Ingresos frente a gastos",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "Comparación de los movimientos financieros registrados.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (totalMovimientos <= 0.0) {

                Text(
                    text = "Todavía no hay movimientos financieros para mostrar.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

            } else {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp),
                    contentAlignment = Alignment.Center
                ) {

                    Canvas(
                        modifier = Modifier.size(210.dp)
                    ) {

                        val grosor = 28.dp.toPx()

                        // Circunferencia de fondo
                        drawArc(
                            color = colorFondo,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(
                                width = grosor
                            )
                        )

                        // Sector de ingresos
                        drawArc(
                            color = colorIngresos,
                            startAngle = -90f,
                            sweepAngle = anguloIngresos,
                            useCenter = false,
                            style = Stroke(
                                width = grosor,
                                cap = StrokeCap.Butt
                            )
                        )

                        // Sector de gastos
                        drawArc(
                            color = colorGastos,
                            startAngle = -90f + anguloIngresos,
                            sweepAngle = anguloGastos,
                            useCenter = false,
                            style = Stroke(
                                width = grosor,
                                cap = StrokeCap.Butt
                            )
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {

                        Text(
                            text = "\$${formatoPesos.format(balance)}",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "Balance neto",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Leyenda de ingresos
                LeyendaBalance(
                    nombre = "Ingresos",
                    monto = ingresos,
                    color = colorIngresos
                )

                // Leyenda de gastos
                LeyendaBalance(
                    nombre = "Gastos",
                    monto = gastos,
                    color = colorGastos
                )

                Text(
                    text = estadoBalance,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Los sectores representan la proporción de ingresos y gastos respecto del total de movimientos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LeyendaBalance(
    nombre: String,
    monto: Long,
    color: Color
) {

    val formatoPesos = NumberFormat.getIntegerInstance(
        Locale("es", "CL")
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(14.dp)
                    .padding(1.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier.size(12.dp)
                ) {
                    drawCircle(color = color)
                }
            }

            Text(
                text = nombre,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Text(
            text = "\$${formatoPesos.format(monto)}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
