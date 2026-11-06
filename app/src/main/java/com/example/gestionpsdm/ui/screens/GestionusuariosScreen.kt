
package com.example.gestionpsdm.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gestionpsdm.model.RolUsuario
import com.example.gestionpsdm.model.Usuario
import com.example.gestionpsdm.viewmodel.GestionUsuariosViewModel

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
        mostrarFormulario = true
    }

    fun abrirEditar(usuario: Usuario) {
        usuarioEditando = usuario
        nombre = usuario.nombre
        correo = usuario.correo
        contrasena = ""
        rolSeleccionado = usuario.rol
        mensajeError = null
        mostrarFormulario = true
    }

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
            Text("Administra las cuentas de la junta de vecinos.")
        }

        item {
            Button(
                onClick = {
                    abrirCrear()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Crear usuario")
            }
        }

        if (mensajeGeneral != null) {
            item {
                Text(
                    text = mensajeGeneral ?: "",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        items(
            items = usuarios,
            key = { usuario -> usuario.id }
        ) { usuario ->

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
                        if (usuario.activo) {
                            "Estado: Activo"
                        } else {
                            "Estado: Inactivo"
                        }
                    )

                    OutlinedButton(
                        onClick = {
                            abrirEditar(usuario)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Editar usuario")
                    }

                    Text("Cuenta habilitada")

                    Switch(
                        checked = usuario.activo,
                        onCheckedChange = { nuevoEstado ->

                            val resultado =
                                viewModel.cambiarEstadoUsuario(
                                    usuario.id,
                                    nuevoEstado
                                )

                            mensajeGeneral = if (resultado) {
                                "Estado de cuenta actualizado."
                            } else {
                                "No se pudo cambiar el estado. Debe existir al menos un administrador activo."
                            }
                        }
                    )
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
                    if (usuarioEditando == null) {
                        "Crear usuario"
                    } else {
                        "Editar usuario"
                    }
                )
            },

            text = {

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = {
                            nombre = it
                            mensajeError = null
                        },
                        label = { Text("Nombre") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = correo,
                        onValueChange = {
                            correo = it
                            mensajeError = null
                        },
                        label = { Text("Correo") },
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
                            label = {
                                Text("Contraseña (mínimo 8 caracteres)")
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("Rol del usuario")

                        Box {

                            OutlinedButton(
                                onClick = {
                                    menuRolesAbierto = true
                                }
                            ) {
                                Text(rolSeleccionado.name)
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
                                            Text(rol.name)
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

                        Text(
                            text = mensajeError ?: "",
                            color = MaterialTheme.colorScheme.error
                        )
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
                    Text("Guardar")
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
            }
        )
    }
}
