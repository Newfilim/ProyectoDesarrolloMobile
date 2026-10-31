
package com.example.gestionpsdm.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gestionpsdm.model.RolUsuario
import com.example.gestionpsdm.model.Usuario

@Composable
fun GestionUsuariosScreen(
    modifier: Modifier = Modifier
) {
    // Datos de ejemplo, todavía no guardados en SQLite.
    val usuarios = listOf(
        Usuario(
            id = 1,
            nombre = "Residente de ejemplo",
            correo = "residente@ejemplo.cl",
            rol = RolUsuario.RESIDENTE,
            activo = true
        ),
        Usuario(
            id = 2,
            nombre = "Tesorería de ejemplo",
            correo = "tesoreria@ejemplo.cl",
            rol = RolUsuario.TESORERIA,
            activo = true
        )
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Gestión de usuarios",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        item {
            Text(
                text = "El administrador podrá crear usuarios y gestionar sus cuentas."
            )
        }

        item {
            Button(
                onClick = {},
                enabled = false
            ) {
                Text("Crear usuario")
            }
        }

        items(usuarios) { usuario ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = usuario.nombre,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text("Correo: ${usuario.correo}")
                    Text("Rol: ${usuario.rol.name}")

                    Text(
                        text = if (usuario.activo) {
                            "Estado: Activo"
                        } else {
                            "Estado: Inactivo"
                        }
                    )

                    Button(
                        onClick = {},
                        enabled = false
                    ) {
                        Text("Editar usuario")
                    }
                }
            }
        }
    }
}
