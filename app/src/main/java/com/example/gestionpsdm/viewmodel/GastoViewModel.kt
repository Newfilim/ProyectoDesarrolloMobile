
package com.example.gestionpsdm.viewmodel

import androidx.lifecycle.ViewModel
import com.example.gestionpsdm.model.CategoriaGasto
import com.example.gestionpsdm.model.GastoErrores
import com.example.gestionpsdm.model.GastoFormularioUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GastoViewModel : ViewModel() {

    private val _estado = MutableStateFlow(GastoFormularioUiState())
    private val _errores = MutableStateFlow(GastoErrores())

    val estado: StateFlow<GastoFormularioUiState> = _estado
    val errores: StateFlow<GastoErrores> = _errores

    fun actualizarDescripcion(nuevaDescripcion: String) {
        _estado.value = _estado.value.copy(
            descripcion = nuevaDescripcion,
            formularioValido = false
        )
        _errores.value = _errores.value.copy(descripcion = null)
    }

    fun actualizarMonto(nuevoMonto: String) {
        _estado.value = _estado.value.copy(
            monto = nuevoMonto,
            formularioValido = false
        )
        _errores.value = _errores.value.copy(monto = null)
    }

    fun actualizarFecha(nuevaFecha: String) {
        _estado.value = _estado.value.copy(
            fecha = nuevaFecha,
            formularioValido = false
        )
        _errores.value = _errores.value.copy(fecha = null)
    }

    fun actualizarCategoria(nuevaCategoria: CategoriaGasto) {
        _estado.value = _estado.value.copy(
            categoria = nuevaCategoria,
            formularioValido = false
        )
        _errores.value = _errores.value.copy(categoria = null)
    }

    fun validarFormulario(): Boolean {

        val datos = _estado.value

        var errorDescripcion: String? = null
        var errorMonto: String? = null
        var errorFecha: String? = null
        var errorCategoria: String? = null

        // Validar descripción
        if (datos.descripcion.isBlank()) {
            errorDescripcion = "La descripción es obligatoria"
        } else if (datos.descripcion.trim().length < 5) {
            errorDescripcion = "Ingrese al menos 5 caracteres"
        }

        // Validar monto
        val montoNumerico = datos.monto.toIntOrNull()

        if (datos.monto.isBlank()) {
            errorMonto = "El monto es obligatorio"
        } else if (montoNumerico == null) {
            errorMonto = "Ingrese un monto entero válido"
        } else if (montoNumerico <= 0) {
            errorMonto = "El monto debe ser mayor que cero"
        }

        // Validar fecha
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

        // Validar categoría
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
