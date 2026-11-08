
package com.example.gestionpsdm.viewmodel

import androidx.lifecycle.ViewModel
import com.example.gestionpsdm.repository.Ingreso
import com.example.gestionpsdm.repository.IngresosRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class IngresoViewModel : ViewModel() {

    fun obtenerIngresos(): List<Ingreso> {
        return IngresosRepository.ingresos
    }

    fun obtenerTotalIngresos(): Long {
        return IngresosRepository.obtenerTotalIngresos()
    }

    fun registrarIngreso(
        descripcion: String,
        monto: String,
        fecha: String
    ): String? {

        if (descripcion.isBlank()) {
            return "La descripción es obligatoria."
        }

        val montoNumerico = monto.toIntOrNull()

        if (montoNumerico == null || montoNumerico <= 0) {
            return "Ingresa un monto entero mayor que cero."
        }

        val patronFecha = Regex("""\d{2}/\d{2}/\d{4}""")

        if (!patronFecha.matches(fecha)) {
            return "La fecha debe tener formato DD/MM/AAAA."
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
            return "Ingresa una fecha válida."
        }

        if (fechaIngresada.after(Date())) {
            return "No puedes registrar ingresos con fecha futura."
        }

        val resultado = IngresosRepository.agregarIngreso(
            descripcion = descripcion.trim(),
            monto = montoNumerico,
            fecha = fecha
        )

        if (!resultado) {
            return "No se pudo registrar el ingreso."
        }

        return null
    }
}
