
package com.example.gestionpsdm.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.example.gestionpsdm.model.LoginUiState
import com.example.gestionpsdm.model.Usuario
import com.example.gestionpsdm.repository.UsuariosDemoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LoginViewModel : ViewModel() {

    private val _estado = MutableStateFlow(LoginUiState())

    val estado: StateFlow<LoginUiState> = _estado

    fun actualizarCorreo(nuevoCorreo: String) {
        _estado.value = _estado.value.copy(
            correo = nuevoCorreo,
            errorCorreo = null,
            errorGeneral = null
        )
    }

    fun actualizarContrasena(nuevaContrasena: String) {
        _estado.value = _estado.value.copy(
            contrasena = nuevaContrasena,
            errorContrasena = null,
            errorGeneral = null
        )
    }

    fun iniciarSesion(): Usuario? {

        val datos = _estado.value

        var errorCorreo: String? = null
        var errorContrasena: String? = null

        if (datos.correo.isBlank()) {
            errorCorreo = "El correo es obligatorio"
        } else if (!Patterns.EMAIL_ADDRESS.matcher(
                datos.correo.trim()
            ).matches()
        ) {
            errorCorreo = "Ingrese un correo válido"
        }

        if (datos.contrasena.isBlank()) {
            errorContrasena = "La contraseña es obligatoria"
        }

        _estado.value = datos.copy(
            errorCorreo = errorCorreo,
            errorContrasena = errorContrasena,
            errorGeneral = null
        )

        if (errorCorreo != null || errorContrasena != null) {
            return null
        }

        val usuario = UsuariosDemoRepository.iniciarSesion(
            datos.correo,
            datos.contrasena
        )

        if (usuario == null) {
            _estado.value = _estado.value.copy(
                errorGeneral = "Correo o contraseña incorrectos"
            )
        }

        return usuario
    }
}
