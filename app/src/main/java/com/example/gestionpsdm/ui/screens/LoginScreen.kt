package com.example.gestionpsdm.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.gestionpsdm.model.Usuario
import com.example.gestionpsdm.viewmodel.LoginViewModel
import com.example.gestionpsdm.ui.components.MensajeError

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onInicioCorrecto: (Usuario) -> Unit,
    modifier: Modifier = Modifier
) {

    val estado by viewModel.estado.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            12.dp,
            Alignment.CenterVertically
        )
    ) {

        Text(
            text = "Junta de Vecinos Zaror",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Inicia sesión para continuar",
            style = MaterialTheme.typography.bodyLarge
        )

        OutlinedTextField(
            value = estado.correo,
            onValueChange = {
                viewModel.actualizarCorreo(it)
            },
            label = {
                Text("Correo electrónico")
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            singleLine = true,
            isError = estado.errorCorreo != null,
            supportingText = {
                MensajeError(estado.errorCorreo)
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = estado.contrasena,
            onValueChange = {
                viewModel.actualizarContrasena(it)
            },
            label = {
                Text("Contraseña")
            },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            singleLine = true,
            isError = estado.errorContrasena != null,
            supportingText = {
                MensajeError(estado.errorCorreo)
            },
            modifier = Modifier.fillMaxWidth()
        )

        MensajeError(estado.errorGeneral)

        Button(
            onClick = {
                val usuario = viewModel.iniciarSesion()

                if (usuario != null) {
                    onInicioCorrecto(usuario)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Iniciar sesión")
        }

        Text(
            text = "Las cuentas son creadas por el administrador.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
