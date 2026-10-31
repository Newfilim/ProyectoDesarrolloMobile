
package com.example.gestionpsdm.repository

import com.example.gestionpsdm.model.RolUsuario
import com.example.gestionpsdm.model.Usuario

private data class CuentaDemo(
    val usuario: Usuario,
    val contrasena: String
)

object UsuariosDemoRepository {

    // Cuentas ficticias y temporales, solo para desarrollo.
    private val cuentas = listOf(
        CuentaDemo(
            Usuario(
                id = 1,
                nombre = "Residente de prueba",
                correo = "residente@zaror.cl",
                rol = RolUsuario.RESIDENTE,
                activo = true
            ),
            "Demo1234!"
        ),
        CuentaDemo(
            Usuario(
                id = 2,
                nombre = "Administrador de prueba",
                correo = "admin@zaror.cl",
                rol = RolUsuario.ADMINISTRADOR,
                activo = true
            ),
            "Demo1234!"
        ),
        CuentaDemo(
            Usuario(
                id = 3,
                nombre = "Tesorería de prueba",
                correo = "tesoreria@zaror.cl",
                rol = RolUsuario.TESORERIA,
                activo = true
            ),
            "Demo1234!"
        ),
        CuentaDemo(
            Usuario(
                id = 4,
                nombre = "Directiva de prueba",
                correo = "directiva@zaror.cl",
                rol = RolUsuario.DIRECTIVA,
                activo = true
            ),
            "Demo1234!"
        )
    )

    fun iniciarSesion(
        correo: String,
        contrasena: String
    ): Usuario? {

        for (cuenta in cuentas) {

            if (cuenta.usuario.correo.equals(
                    correo.trim(),
                    ignoreCase = true
                ) &&
                cuenta.contrasena == contrasena &&
                cuenta.usuario.activo
            ) {
                return cuenta.usuario
            }
        }

        return null
    }
}
