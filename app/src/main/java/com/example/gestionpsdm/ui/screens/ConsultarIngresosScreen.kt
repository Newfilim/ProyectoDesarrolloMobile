
package com.example.gestionpsdm.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.gestionpsdm.repository.IngresosRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ConsultarIngresosScreen(
    modifier: Modifier = Modifier
) {

    var descripcion by remember { mutableStateOf("") }
    var monto by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var esError by remember { mutableStateOf(false) }

    val ingresos = IngresosRepository.ingresos
    val totalIngresos = IngresosRepository.obtenerTotalIngresos()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        item {
            Text(
                text = "Registro de ingresos",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        item {
            Text("Registra manualmente los ingresos de la comunidad.")
        }

        item {
            OutlinedTextField(
                value = descripcion,
                onValueChange = {
                    descripcion = it
                    mensaje = ""
                },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = monto,
                onValueChange = {
                    monto = it
                    mensaje = ""
                },
                label = { Text("Monto en pesos") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = fecha,
                onValueChange = {
                    fecha = it
                    mensaje = ""
                },
                label = { Text("Fecha (DD/MM/AAAA)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Button(
                onClick = {

                    val montoNumerico = monto.toIntOrNull()

                    val formato = SimpleDateFormat(
                        "dd/MM/yyyy",
                        Locale.forLanguageTag("es-CL")
                    )
                    formato.isLenient = false

                    val fechaIngresada = try {
                        formato.parse(fecha)
                    } catch (e: Exception) {
                        null
                    }

                    val fechaHoy = formato.parse(
                        formato.format(Date())
                    )

                    if (descripcion.isBlank() ||
                        montoNumerico == null ||
                        montoNumerico <= 0 ||
                        fechaIngresada == null ||
                        fechaIngresada.after(fechaHoy)
                    ) {
                        mensaje = "Revisa la descripción, monto y fecha. No se permiten fechas futuras."
                        esError = true
                    } else {

                        val resultado = IngresosRepository.agregarIngreso(
                            descripcion = descripcion,
                            monto = montoNumerico,
                            fecha = fecha
                        )

                        if (resultado) {
                            descripcion = ""
                            monto = ""
                            fecha = ""
                            mensaje = "Ingreso registrado correctamente."
                            esError = false
                        } else {
                            mensaje = "No se pudo registrar el ingreso."
                            esError = true
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Registrar ingreso")
            }
        }

        if (mensaje.isNotEmpty()) {
            item {
                Text(
                    text = mensaje,
                    color = if (esError) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            }
        }

        item {
            Text(
                text = "Total de ingresos: $${totalIngresos} CLP",
                style = MaterialTheme.typography.titleMedium
            )
        }

        item {
            Text(
                text = "Ingresos registrados",
                style = MaterialTheme.typography.titleMedium
            )
        }

        if (ingresos.isEmpty()) {
            item {
                Text("Todavía no hay ingresos registrados.")
            }
        }

        items(
            items = ingresos,
            key = { it.id }
        ) { ingreso ->

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = ingreso.descripcion,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text("Fecha: ${ingreso.fecha}")
                    Text("Monto: $${ingreso.monto} CLP")
                }
            }
        }
    }
}

