package com.example.gestionpsdm.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gestionpsdm.viewmodel.ReservaViewModel

@Composable
fun SolicitarReservaScreen(
    viewModel: ReservaViewModel,
    modifier: Modifier = Modifier
) {
    val estado by viewModel.estado.collectAsState()
    val errores by viewModel.errores.collectAsState()

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Solicitar reserva",
            style = MaterialTheme.typography.titleLarge
        )

        OutlinedTextField(
            value = estado.fecha,
            onValueChange = { nuevaFecha ->
                viewModel.actualizarFecha(nuevaFecha)
            },
            label = { Text("Fecha (DD/MM/AAAA)") },
            isError = errores.fecha != null,
            supportingText= {
                if(errores.fecha != null){
                    Text(errores.fecha!!)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = estado.horaInicio,
            onValueChange = { nuevaHora ->
                viewModel.actualizarHoraInicio(nuevaHora)
            },
            label = { Text("Hora de inicio") },
            isError = errores.horaInicio != null,
            supportingText = {
                if(errores.horaInicio != null){
                    Text(errores.horaInicio!!)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = estado.horaFin,
            onValueChange = { nuevaHora ->
                viewModel.actualizarHoraFin(nuevaHora)
            },
            isError = errores.horaFin != null,
            supportingText = {
                if(errores.horaFin != null){
                    Text(errores.horaFin!!)
                }
            },
            label = { Text("Hora de término") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                viewModel.validarFormulario()
            }
        ) {
            Text("Validar solicitud")
        }

        // Aquí agregarás los demás campos.
    }
}