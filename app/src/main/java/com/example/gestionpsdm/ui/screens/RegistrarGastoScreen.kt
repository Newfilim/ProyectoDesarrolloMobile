
package com.example.gestionpsdm.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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

    var menuAbierto by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "Registrar gasto",
            style = MaterialTheme.typography.headlineMedium
        )

        Text("Ingresa los datos del gasto comunitario.")

        OutlinedTextField(
            value = estado.descripcion,
            onValueChange = {
                viewModel.actualizarDescripcion(it)
            },
            label = { Text("Descripción") },
            isError = errores.descripcion != null,
            supportingText = {
                MensajeError(errores.descripcion)
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = estado.monto,
            onValueChange = {
                viewModel.actualizarMonto(it)
            },
            label = { Text("Monto en pesos (sin puntos)") },
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

        OutlinedTextField(
            value = estado.fecha,
            onValueChange = {
                viewModel.actualizarFecha(it)
            },
            label = { Text("Fecha (DD/MM/AAAA)") },
            singleLine = true,
            isError = errores.fecha != null,
            supportingText = {
                MensajeError(errores.fecha)
            },
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Categoría del gasto",
            style = MaterialTheme.typography.titleMedium
        )

        Box {
            Button(
                onClick = {
                    menuAbierto = true
                }
            ) {
                Text(
                    estado.categoria?.name
                        ?: "Seleccionar categoría"
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
                            Text(categoria.name)
                        },
                        onClick = {
                            viewModel.actualizarCategoria(categoria)
                            menuAbierto = false
                        }
                    )
                }
            }
        }

        MensajeError(errores.categoria)

        Button(
            onClick = {
                viewModel.validarFormulario()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Validar gasto")
        }

        if (estado.formularioValido) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Datos correctos",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text("Descripción: ${estado.descripcion}")
                    Text("Monto: $${estado.monto}")
                    Text("Fecha: ${estado.fecha}")
                    Text("Categoría: ${estado.categoria?.name}")

                    Text(
                        text = "Formulario validado. El almacenamiento se implementará posteriormente.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
