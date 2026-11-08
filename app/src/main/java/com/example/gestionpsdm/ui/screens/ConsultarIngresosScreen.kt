
package com.example.gestionpsdm.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions

import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gestionpsdm.viewmodel.IngresoViewModel

import java.text.NumberFormat
import java.util.Locale

@Composable
fun ConsultarIngresosScreen(
    modifier: Modifier = Modifier,
    viewModel: IngresoViewModel = viewModel()
) {

    var descripcion by remember {
        mutableStateOf("")
    }

    var monto by remember {
        mutableStateOf("")
    }

    var fecha by remember {
        mutableStateOf("")
    }

    var mensaje by remember {
        mutableStateOf("")
    }

    var esError by remember {
        mutableStateOf(false)
    }

    val ingresos = viewModel.obtenerIngresos()
    val totalIngresos = viewModel.obtenerTotalIngresos()

    // Formato de números para Chile: 25000 -> 25.000
    val formatoPesos = NumberFormat.getIntegerInstance(
        Locale("es", "CL")
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),

        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // TITULO

        item {
            Text(
                text = "Registro de ingresos",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Text(
                text = "Registra los ingresos recibidos por la comunidad y consulta los movimientos registrados.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // RESUMEN DEL TOTAL

        item {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),

                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {

                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Text(
                        text = "Total de ingresos registrados",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "\$${formatoPesos.format(totalIngresos)}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Pesos chilenos (CLP)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // FORMULARIO DE INGRESOS

        item {

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
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    Text(
                        text = "Nuevo ingreso",
                        style = MaterialTheme.typography.titleLarge
                    )

                    // PASO 1: DESCRIPCION

                    Text(
                        text = "1. Descripción del ingreso",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "Indica de dónde proviene el dinero recibido.",
                        style = MaterialTheme.typography.bodyMedium,
                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = descripcion,

                        onValueChange = {
                            descripcion = it
                            mensaje = ""
                        },

                        label = {
                            Text("Descripción")
                        },

                        placeholder = {
                            Text("Ej.: Arriendo del quincho")
                        },

                        modifier = Modifier.fillMaxWidth()
                    )

                    HorizontalDivider()

                    // PASO 2: MONTO

                    Text(
                        text = "2. Monto del ingreso",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "Ingresa la cantidad recibida en pesos, sin puntos ni comas.",
                        style = MaterialTheme.typography.bodyMedium,
                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = monto,

                        onValueChange = {
                            monto = it
                            mensaje = ""
                        },

                        label = {
                            Text("Monto en pesos")
                        },

                        placeholder = {
                            Text("Ej.: 25000")
                        },

                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),

                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    HorizontalDivider()

                    // PASO 3: FECHA

                    Text(
                        text = "3. Fecha del ingreso",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "Escribe la fecha utilizando el formato DD/MM/AAAA.",
                        style = MaterialTheme.typography.bodyMedium,
                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = fecha,

                        onValueChange = {
                            fecha = it
                            mensaje = ""
                        },

                        label = {
                            Text("Fecha (DD/MM/AAAA)")
                        },

                        placeholder = {
                            Text("Ej.: 08/11/2026")
                        },

                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // BOTON GUARDAR

        item {

            Button(
                onClick = {

                    val error = viewModel.registrarIngreso(
                        descripcion = descripcion,
                        monto = monto,
                        fecha = fecha
                    )

                    if (error == null) {

                        descripcion = ""
                        monto = ""
                        fecha = ""

                        mensaje = "Ingreso registrado correctamente."
                        esError = false

                    } else {

                        mensaje = error
                        esError = true
                    }
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),

                shape = RoundedCornerShape(14.dp)
            ) {

                Text(
                    text = "✓ Guardar ingreso",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        // MENSAJE DE RESULTADO

        if (mensaje.isNotEmpty()) {

            item {

                Surface(
                    shape = RoundedCornerShape(14.dp),

                    color = if (esError) {
                        MaterialTheme.colorScheme.errorContainer
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    }
                ) {

                    Text(
                        text = mensaje,
                        modifier = Modifier.padding(16.dp),

                        style = MaterialTheme.typography.bodyLarge,

                        color = if (esError) {
                            MaterialTheme.colorScheme.onErrorContainer
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        }
                    )
                }
            }
        }

        // LISTADO DE INGRESOS

        item {
            Text(
                text = "Ingresos registrados",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (ingresos.isEmpty()) {

            item {
                Text(
                    text = "Todavía no hay ingresos registrados.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

        } else {

            items(
                items = ingresos,
                key = { ingreso -> ingreso.id }
            ) { ingreso ->

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
                            text = ingreso.descripcion,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "Fecha: ${ingreso.fecha}",
                            style = MaterialTheme.typography.bodyLarge,
                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "Monto recibido",
                            style = MaterialTheme.typography.bodyMedium,
                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "\$${formatoPesos.format(ingreso.monto)} CLP",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

