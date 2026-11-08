
package com.example.gestionpsdm.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gestionpsdm.model.RolUsuario
import com.example.gestionpsdm.model.Usuario
import com.example.gestionpsdm.viewmodel.GestionUsuariosViewModel

// Muestra los roles con nombres fáciles de comprender.
private fun nombreRol(rol: RolUsuario): String {
    return when (rol) {
        RolUsuario.RESIDENTE -> "Residente"
        RolUsuario.ADMINISTRADOR -> "Administrador"
        RolUsuario.TESORERIA -> "Tesorería"
        RolUsuario.DIRECTIVA -> "Directiva"
    }
}

@Composable
fun GestionUsuariosScreen(
    modifier: Modifier = Modifier,
    viewModel: GestionUsuariosViewModel = viewModel()
) {

    val usuarios = viewModel.obtenerUsuarios()

    var mostrarFormulario by remember {
        mutableStateOf(false)
    }

    var usuarioEditando by remember {
        mutableStateOf<Usuario?>(null)
    }

    var nombre by remember {
        mutableStateOf("")
    }

    var correo by remember {
        mutableStateOf("")
    }

    var contrasena by remember {
        mutableStateOf("")
    }

    var rolSeleccionado by remember {
        mutableStateOf(RolUsuario.RESIDENTE)
    }

    var menuRolesAbierto by remember {
        mutableStateOf(false)
    }

    var mensajeError by remember {
        mutableStateOf<String?>(null)
    }

    var mensajeGeneral by remember {
        mutableStateOf<String?>(null)
    }

    fun abrirCrear() {
        usuarioEditando = null
        nombre = ""
        correo = ""
        contrasena = ""
        rolSeleccionado = RolUsuario.RESIDENTE
        mensajeError = null
        menuRolesAbierto = false
        mostrarFormulario = true
    }

    fun abrirEditar(usuario: Usuario) {
        usuarioEditando = usuario
        nombre = usuario.nombre
        correo = usuario.correo
        contrasena = ""
        rolSeleccionado = usuario.rol
        mensajeError = null
        menuRolesAbierto = false
        mostrarFormulario = true
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item {
            Text(
                text = "Gestión de usuarios",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Text(
                text = "Crea cuentas, edita los datos y administra el acceso de los integrantes de la comunidad.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Button(
                onClick = {
                    abrirCrear()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "+ Crear nuevo usuario",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        if (mensajeGeneral != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = mensajeGeneral ?: "",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        if (usuarios.isEmpty()) {
            item {
                Text(
                    text = "No hay usuarios registrados.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        items(
            items = usuarios,
            key = { usuario -> usuario.id }
        ) { usuario ->

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
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    Text(
                        text = usuario.nombre,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Text(
                            text = if (usuario.activo) {
                                "✓ Cuenta activa"
                            } else {
                                "Cuenta deshabilitada"
                            },
                            modifier = Modifier.padding(
                                horizontal = 14.dp,
                                vertical = 10.dp
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Correo electrónico",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = usuario.correo,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Rol del usuario",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = nombreRol(usuario.rol),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {

                            Text(
                                text = "Permitir acceso",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                text = if (usuario.activo) {
                                    "Esta cuenta puede iniciar sesión."
                                } else {
                                    "Esta cuenta no puede iniciar sesión."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color =
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = usuario.activo,
                            onCheckedChange = { nuevoEstado ->

                                val resultado =
                                    viewModel.cambiarEstadoUsuario(
                                        usuario.id,
                                        nuevoEstado
                                    )

                                mensajeGeneral = if (resultado) {

                                    if (nuevoEstado) {
                                        "Cuenta habilitada correctamente."
                                    } else {
                                        "Cuenta deshabilitada correctamente."
                                    }

                                } else {

                                    "No se pudo cambiar el estado. Debe existir al menos un administrador activo."
                                }
                            }
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            abrirEditar(usuario)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = "Editar datos del usuario",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }

    if (mostrarFormulario) {

        AlertDialog(
            onDismissRequest = {
                mostrarFormulario = false
            },

            title = {
                Text(
                    text = if (usuarioEditando == null) {
                        "Crear usuario"
                    } else {
                        "Editar usuario"
                    },
                    style = MaterialTheme.typography.titleLarge
                )
            },

            text = {

                Column(
                    modifier = Modifier
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Text(
                        text = if (usuarioEditando == null) {
                            "Completa los datos para crear una cuenta."
                        } else {
                            "Modifica el nombre o correo de este usuario."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = {
                            nombre = it
                            mensajeError = null
                        },
                        label = { Text("Nombre completo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = correo,
                        onValueChange = {
                            correo = it
                            mensajeError = null
                        },
                        label = { Text("Correo electrónico") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (usuarioEditando == null) {

                        OutlinedTextField(
                            value = contrasena,
                            onValueChange = {
                                contrasena = it
                                mensajeError = null
                            },
                            label = { Text("Contraseña") },
                            supportingText = {
                                Text("Debe tener al menos 8 caracteres.")
                            },
                            visualTransformation =
                                PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "Rol del nuevo usuario",
                            style = MaterialTheme.typography.titleMedium
                        )

                        androidx.compose.foundation.layout.Box {

                            OutlinedButton(
                                onClick = {
                                    menuRolesAbierto = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 52.dp)
                            ) {
                                Text(
                                    text = "${nombreRol(rolSeleccionado)}  ▾",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }

                            DropdownMenu(
                                expanded = menuRolesAbierto,
                                onDismissRequest = {
                                    menuRolesAbierto = false
                                }
                            ) {

                                for (rol in RolUsuario.entries) {

                                    DropdownMenuItem(
                                        text = {
                                            Text(nombreRol(rol))
                                        },
                                        onClick = {
                                            rolSeleccionado = rol
                                            menuRolesAbierto = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (mensajeError != null) {

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = mensajeError ?: "",
                                modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            },

            confirmButton = {

                Button(
                    onClick = {

                        val usuario = usuarioEditando

                        val resultado = if (usuario == null) {

                            viewModel.crearUsuario(
                                nombre = nombre,
                                correo = correo,
                                contrasena = contrasena,
                                rol = rolSeleccionado
                            )

                        } else {

                            viewModel.editarUsuario(
                                id = usuario.id,
                                nombre = nombre,
                                correo = correo
                            )
                        }

                        if (resultado) {

                            mensajeGeneral = if (usuario == null) {
                                "Usuario creado correctamente."
                            } else {
                                "Usuario actualizado correctamente."
                            }

                            mostrarFormulario = false

                        } else {

                            mensajeError =
                                "Revisa los datos. El correo debe ser válido y único; para crear una cuenta, la contraseña debe tener al menos 8 caracteres."
                        }
                    }
                ) {
                    Text(
                        text = if (usuarioEditando == null) {
                            "Crear cuenta"
                        } else {
                            "Guardar cambios"
                        }
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        mostrarFormulario = false
                    }
                ) {
                    Text("Cancelar")
                }
            },

            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    }
}
