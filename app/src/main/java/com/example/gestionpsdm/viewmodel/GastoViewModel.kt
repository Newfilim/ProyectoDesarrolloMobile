
package com.example.gestionpsdm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionpsdm.model.CategoriaGasto
import com.example.gestionpsdm.model.GastoErrores
import com.example.gestionpsdm.model.GastoFormularioUiState
import com.example.gestionpsdm.repository.GastosRoomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GastoViewModel(
    private val repository: GastosRoomRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(GastoFormularioUiState())
    private val _errores = MutableStateFlow(GastoErrores())

    val estado: StateFlow<GastoFormularioUiState> = _estado
    val errores: StateFlow<GastoErrores> = _errores

    private val _guardando = MutableStateFlow(false)
    val guardando: StateFlow<Boolean> = _guardando

    private val _gastoGuardado = MutableStateFlow(false)
    val gastoGuardado: StateFlow<Boolean> = _gastoGuardado

    private val _errorGuardado = MutableStateFlow<String?>(null)
    val errorGuardado: StateFlow<String?> = _errorGuardado

    fun limpiarMensajeGuardado() {
        _gastoGuardado.value = false
        _errorGuardado.value = null
    }

    fun actualizarDescripcion(nuevaDescripcion: String) {
        if (_guardando.value) return

        _estado.value = _estado.value.copy(
            descripcion = nuevaDescripcion,
            formularioValido = false
        )
        _errores.value = _errores.value.copy(descripcion = null)
        limpiarMensajeGuardado()
    }

    fun actualizarMonto(nuevoMonto: String) {
        if (_guardando.value) return

        _estado.value = _estado.value.copy(
            monto = nuevoMonto,
            formularioValido = false
        )
        _errores.value = _errores.value.copy(monto = null)
        limpiarMensajeGuardado()
    }

    fun actualizarFecha(nuevaFecha: String) {
        if (_guardando.value) return

        _estado.value = _estado.value.copy(
            fecha = nuevaFecha,
            formularioValido = false
        )
        _errores.value = _errores.value.copy(fecha = null)
        limpiarMensajeGuardado()
    }

    fun actualizarCategoria(nuevaCategoria: CategoriaGasto) {
        if (_guardando.value) return

        _estado.value = _estado.value.copy(
            categoria = nuevaCategoria,
            formularioValido = false
        )
        _errores.value = _errores.value.copy(categoria = null)
        limpiarMensajeGuardado()
    }

    fun validarFormulario(): Boolean {

        val datos = _estado.value

        var errorDescripcion: String? = null
        var errorMonto: String? = null
        var errorFecha: String? = null
        var errorCategoria: String? = null

        // Descripción
        if (datos.descripcion.isBlank()) {
            errorDescripcion = "La descripción es obligatoria"
        } else if (datos.descripcion.trim().length < 5) {
            errorDescripcion = "Ingrese al menos 5 caracteres"
        }

        // Monto
        val montoNumerico = datos.monto.toIntOrNull()

        if (datos.monto.isBlank()) {
            errorMonto = "El monto es obligatorio"
        } else if (montoNumerico == null) {
            errorMonto = "Ingrese un monto entero válido"
        } else if (montoNumerico <= 0) {
            errorMonto = "El monto debe ser mayor que cero"
        }

        // Fecha
        val fechaIngresada = convertirFecha(datos.fecha)

        if (datos.fecha.isBlank()) {
            errorFecha = "La fecha es obligatoria"
        } else if (fechaIngresada == null) {
            errorFecha = "Ingrese una fecha válida (DD/MM/AAAA)"
        } else {
            val formato = SimpleDateFormat(
                "dd/MM/yyyy",
                Locale.forLanguageTag("es-CL")
            )

            val fechaHoy = convertirFecha(formato.format(Date()))

            if (fechaHoy != null && fechaIngresada.after(fechaHoy)) {
                errorFecha = "No puede registrar un gasto futuro"
            }
        }

        // Categoría
        if (datos.categoria == null) {
            errorCategoria = "Seleccione una categoría"
        }

        _errores.value = GastoErrores(
            descripcion = errorDescripcion,
            monto = errorMonto,
            fecha = errorFecha,
            categoria = errorCategoria
        )

        val valido = errorDescripcion == null &&
                errorMonto == null &&
                errorFecha == null &&
                errorCategoria == null

        _estado.value = _estado.value.copy(
            formularioValido = valido
        )

        return valido
    }

    fun registrarGasto() {

        // Evita registros duplicados por pulsaciones repetidas.
        if (_guardando.value) return

        limpiarMensajeGuardado()

        if (!validarFormulario()) {
            return
        }

        val datos = _estado.value
        val monto = datos.monto.toIntOrNull() ?: return
        val categoria = datos.categoria ?: return

        _guardando.value = true

        viewModelScope.launch {
            try {
                repository.agregarGasto(
                    descripcion = datos.descripcion.trim(),
                    monto = monto,
                    fecha = datos.fecha,
                    categoria = categoria
                )

                // Limpiar después de guardar correctamente en SQLite.
                _estado.value = GastoFormularioUiState()
                _errores.value = GastoErrores()
                _gastoGuardado.value = true

            } catch (e: Exception) {
                _errorGuardado.value =
                    "No se pudo guardar el gasto. Inténtalo nuevamente."

            } finally {
                _guardando.value = false
            }
        }
    }

    private fun convertirFecha(fecha: String): Date? {

        val patron = Regex("""\d{2}/\d{2}/\d{4}""")

        if (!patron.matches(fecha)) {
            return null
        }

        val formato = SimpleDateFormat(
            "dd/MM/yyyy",
            Locale.forLanguageTag("es-CL")
        )

        formato.isLenient = false

        return try {
            formato.parse(fecha)
        } catch (e: ParseException) {
            null
        }
    }
}
