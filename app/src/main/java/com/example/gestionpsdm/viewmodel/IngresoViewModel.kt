
package com.example.gestionpsdm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionpsdm.model.Ingreso
import com.example.gestionpsdm.repository.IngresosRoomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class IngresoViewModel(
    private val repository: IngresosRoomRepository
) : ViewModel() {

    // Ingresos observados desde Room
    val ingresos: StateFlow<List<Ingreso>> =
        repository.obtenerIngresos().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Total actualizado cuando cambian los datos
    val totalIngresos: StateFlow<Long> =
        repository.obtenerTotalIngresos().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0L
        )

    private val _registrando = MutableStateFlow(false)
    val registrando: StateFlow<Boolean> = _registrando

    fun registrarIngreso(
        descripcion: String,
        monto: String,
        fecha: String,
        onResultado: (String?) -> Unit
    ) {

        if (_registrando.value) {
            return
        }

        // Validar descripción
        if (descripcion.isBlank()) {
            onResultado("La descripción es obligatoria.")
            return
        }

        // Validar monto
        val montoNumerico = monto.toIntOrNull()

        if (montoNumerico == null || montoNumerico <= 0) {
            onResultado("Ingresa un monto entero mayor que cero.")
            return
        }

        // Validar fecha
        val patronFecha = Regex("""\d{2}/\d{2}/\d{4}""")

        if (!patronFecha.matches(fecha)) {
            onResultado("La fecha debe tener formato DD/MM/AAAA.")
            return
        }

        val formato = SimpleDateFormat(
            "dd/MM/yyyy",
            Locale.forLanguageTag("es-CL")
        )

        formato.isLenient = false

        val fechaIngresada = try {
            formato.parse(fecha)
        } catch (e: Exception) {
            null
        }

        if (fechaIngresada == null) {
            onResultado("Ingresa una fecha válida.")
            return
        }

        if (fechaIngresada.after(Date())) {
            onResultado("No puedes registrar ingresos con fecha futura.")
            return
        }

        // Registrar en SQLite
        _registrando.value = true

        viewModelScope.launch {

            try {
                repository.agregarIngreso(
                    descripcion = descripcion.trim(),
                    monto = montoNumerico,
                    fecha = fecha
                )

                // null significa registro exitoso
                onResultado(null)

            } catch (e: Exception) {

                onResultado(
                    "No se pudo guardar el ingreso. Inténtalo nuevamente."
                )

            } finally {
                _registrando.value = false
            }
        }
    }
}
