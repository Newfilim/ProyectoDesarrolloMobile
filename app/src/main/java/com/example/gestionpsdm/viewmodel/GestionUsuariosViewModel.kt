
package com.example.gestionpsdm.viewmodel

import androidx.lifecycle.ViewModel
import com.example.gestionpsdm.model.RolUsuario
import com.example.gestionpsdm.model.Usuario
import com.example.gestionpsdm.repository.UsuariosDemoRepository

class GestionUsuariosViewModel : ViewModel() {

    fun obtenerUsuarios(): List<Usuario> {
        return UsuariosDemoRepository.obtenerUsuarios()
    }

    fun crearUsuario(
        nombre: String,
        correo: String,
        contrasena: String,
        rol: RolUsuario
    ): Boolean {
        return UsuariosDemoRepository.crearUsuario(
            nombre,
            correo,
            contrasena,
            rol
        )
    }

    fun editarUsuario(
        id: Int,
        nombre: String,
        correo: String
    ): Boolean {
        return UsuariosDemoRepository.editarUsuario(
            id,
            nombre,
            correo
        )
    }

    fun cambiarEstadoUsuario(
        id: Int,
        activo: Boolean
    ): Boolean {
        return UsuariosDemoRepository.cambiarEstadoUsuario(
            id,
            activo
        )
    }
}
