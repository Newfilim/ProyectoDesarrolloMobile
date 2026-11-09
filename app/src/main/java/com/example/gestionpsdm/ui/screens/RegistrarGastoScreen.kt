
package com.example.gestionpsdm.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

import com.example.gestionpsdm.model.CategoriaGasto
import com.example.gestionpsdm.ui.components.MensajeError
import com.example.gestionpsdm.viewmodel.GastoViewModel

@Composable
fun RegistrarGastoScreen(
    viewModel: GastoViewModel,
    modifier: Modifier = Modifier
) {

    val estado by viewModel.estado.collectAsState()
    val errores by viewModel.errores.collectAsState()
    val guardando by viewModel.guardando.collectAsState()
    val mensajeGuardado by viewModel.gastoGuardado.collectAsState()
    val errorGuardado by viewModel.errorGuardado.collectAsState()

    var menuAbierto by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),

        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Registrar gasto",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Completa los siguientes datos para registrar un gasto de la comunidad.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

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

                // PASO 1: DESCRIPCION

                Text(
                    text = "1. Descripción del gasto",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Indica en qué se utilizó el dinero.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = estado.descripcion,

                    onValueChange = {
                        viewModel.actualizarDescripcion(it)

                    },

                    label = {
                        Text("Descripción")
                    },

                    placeholder = {
                        Text("Ej.: Compra de artículos de limpieza")
                    },

                    isError = errores.descripcion != null,

                    supportingText = {
                        MensajeError(errores.descripcion)
                    },

                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider()

                // PASO 2: MONTO

                Text(
                    text = "2. Monto del gasto",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Ingresa el monto en pesos chilenos, sin puntos ni comas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = estado.monto,

                    onValueChange = {
                        viewModel.actualizarMonto(it)

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
                    isError = errores.monto != null,

                    supportingText = {
                        MensajeError(errores.monto)
                    },

                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider()

                // PASO 3: FECHA

                Text(
                    text = "3. Fecha del gasto",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Escribe la fecha utilizando el formato DD/MM/AAAA.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = estado.fecha,

                    onValueChange = {
                        viewModel.actualizarFecha(it)

                    },

                    label = {
                        Text("Fecha (DD/MM/AAAA)")
                    },

                    placeholder = {
                        Text("Ej.: 08/11/2026")
                    },

                    singleLine = true,
                    isError = errores.fecha != null,

                    supportingText = {
                        MensajeError(errores.fecha)
                    },

                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider()

                // PASO 4: CATEGORIA

                Text(
                    text = "4. Categoría del gasto",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Selecciona la categoría que mejor describa el gasto.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    OutlinedButton(
                        onClick = {
                            menuAbierto = true
                        },

                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp),

                        shape = RoundedCornerShape(14.dp)
                    ) {

                        Text(
                            text = if (estado.categoria == null) {
                                "Seleccionar categoría  ▾"
                            } else {
                                "${estado.categoria!!.name.replace('_', ' ')}  ▾"
                            },

                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    DropdownMenu(
                        expanded = menuAbierto,

                        onDismissRequest = {
                            menuAbierto = false
                        }
                    ) {

                        for (categoria in CategoriaGasto.entries) {

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        categoria.name.replace('_', ' ')
                                    )
                                },

                                onClick = {

                                    viewModel.actualizarCategoria(
                                        categoria
                                    )


                                    menuAbierto = false
                                }
                            )
                        }
                    }
                }

                MensajeError(errores.categoria)
            }
        }

        // BOTON PARA REGISTRAR


        Button(
            onClick = {
                viewModel.registrarGasto()
            },

            enabled = !guardando,

            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),

            shape = RoundedCornerShape(14.dp)
        ) {

            Text(
                text = if (guardando) {
                    "Guardando gasto..."
                } else {
                    "✓ Guardar gasto"
                },

                style = MaterialTheme.typography.labelLarge
            )
        }
        MensajeError(errorGuardado)


        // MENSAJE DE CONFIRMACION

        if (mensajeGuardado) {

            Surface(
                shape = RoundedCornerShape(14.dp),

                color =
                    MaterialTheme.colorScheme.primaryContainer
            ) {

                Text(
                    text = "✓ Gasto registrado correctamente. Puedes consultar el resultado en el informe financiero.",
                    modifier = Modifier.padding(16.dp),

                    style = MaterialTheme.typography.bodyLarge,

                    color =
                        MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
