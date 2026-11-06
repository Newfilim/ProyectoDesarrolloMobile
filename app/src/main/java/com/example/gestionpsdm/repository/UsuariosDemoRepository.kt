
    package com.example.gestionpsdm.repository

    import android.util.Patterns
    import androidx.compose.runtime.mutableStateListOf
    import com.example.gestionpsdm.model.RolUsuario
    import com.example.gestionpsdm.model.Usuario

    private data class CuentaDemo(
        val usuario: Usuario,
        val contrasena: String
    )

    object UsuariosDemoRepository {

        // Cuentas ficticias para la demostración.
        private val cuentas = mutableStateListOf(
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

        fun obtenerUsuarios(): List<Usuario> {
            return cuentas.map { it.usuario }
        }

        fun iniciarSesion(
            correo: String,
            contrasena: String
        ): Usuario? {

            for (cuenta in cuentas) {
                if (
                    cuenta.usuario.correo.equals(
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

        fun crearUsuario(
            nombre: String,
            correo: String,
            contrasena: String,
            rol: RolUsuario
        ): Boolean {

            val nombreLimpio = nombre.trim()
            val correoLimpio = correo.trim()

            if (nombreLimpio.isBlank() ||
                correoLimpio.isBlank() ||
                contrasena.isBlank()
            ) {
                return false
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(correoLimpio).matches()) {
                return false
            }

            if (contrasena.length < 8) {
                return false
            }

            for (cuenta in cuentas) {
                if (cuenta.usuario.correo.equals(
                        correoLimpio,
                        ignoreCase = true
                    )
                ) {
                    return false
                }
            }

            val ultimoId = cuentas.map {
                it.usuario.id
            }.maxOrNull() ?: 0

            val nuevoUsuario = Usuario(
                id = ultimoId + 1,
                nombre = nombreLimpio,
                correo = correoLimpio,
                rol = rol,
                activo = true
            )

            cuentas.add(
                CuentaDemo(
                    usuario = nuevoUsuario,
                    contrasena = contrasena
                )
            )

            return true
        }

        fun editarUsuario(
            id: Int,
            nuevoNombre: String,
            nuevoCorreo: String
        ): Boolean {

            val nombreLimpio = nuevoNombre.trim()
            val correoLimpio = nuevoCorreo.trim()

            if (nombreLimpio.isBlank() || correoLimpio.isBlank()) {
                return false
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(correoLimpio).matches()) {
                return false
            }

            // Comprobar que el correo no pertenezca a otro usuario.
            for (cuenta in cuentas) {
                if (cuenta.usuario.id != id &&
                    cuenta.usuario.correo.equals(
                        correoLimpio,
                        ignoreCase = true
                    )
                ) {
                    return false
                }
            }

            for (i in cuentas.indices) {

                val cuentaActual = cuentas[i]

                if (cuentaActual.usuario.id == id) {

                    val usuarioEditado = cuentaActual.usuario.copy(
                        nombre = nombreLimpio,
                        correo = correoLimpio
                    )

                    cuentas[i] = cuentaActual.copy(
                        usuario = usuarioEditado
                    )

                    return true
                }
            }

            return false
        }

        fun cambiarEstadoUsuario(
            id: Int,
            activo: Boolean
        ): Boolean {

            for (i in cuentas.indices) {

                val cuentaActual = cuentas[i]

                if (cuentaActual.usuario.id == id) {

                    // Evitar deshabilitar al último administrador.
                    if (!activo &&
                        cuentaActual.usuario.rol == RolUsuario.ADMINISTRADOR
                    ) {
                        var administradoresActivos = 0

                        for (cuenta in cuentas) {
                            if (cuenta.usuario.rol == RolUsuario.ADMINISTRADOR &&
                                cuenta.usuario.activo
                            ) {
                                administradoresActivos++
                            }
                        }

                        if (administradoresActivos <= 1) {
                            return false
                        }
                    }

                    val usuarioActualizado = cuentaActual.usuario.copy(
                        activo = activo
                    )

                    cuentas[i] = cuentaActual.copy(
                        usuario = usuarioActualizado
                    )

                    return true
                }
            }

            return false
        }
    }
