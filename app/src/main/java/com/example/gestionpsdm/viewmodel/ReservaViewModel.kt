package com.example.gestionpsdm.viewmodel


import androidx.lifecycle.ViewModel
import com.example.gestionpsdm.model.ReservaFormularioUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.gestionpsdm.model.ReservaErrores
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.text.ParseException
import java.util.Calendar

class ReservaViewModel : ViewModel(){

    private val _estado = MutableStateFlow(
        ReservaFormularioUiState()
    )

    private val _errores = MutableStateFlow(
        ReservaErrores()
    )

    val estado: StateFlow<ReservaFormularioUiState> = _estado
    val errores: StateFlow<ReservaErrores> = _errores
    fun actualizarFecha(nuevaFecha: String) {
        _estado.value = _estado.value.copy(
            fecha = nuevaFecha
        )
    }

    fun actualizarHoraInicio(nuevaHora: String){
        _estado.value = _estado.value.copy(
            horaInicio = nuevaHora
        )
    }

    fun actualizarHoraFin(nuevaHora: String){
        _estado.value = _estado.value.copy(
            horaFin = nuevaHora
        )
    }

    fun validarFormulario(): Boolean {

        val datos = _estado.value

        var errorFecha: String? = null
        var errorHoraInicio: String? = null
        var errorHoraFin: String? = null

        val formatoFecha = SimpleDateFormat(
            "dd/MM/yyyy",
            Locale.forLanguageTag("es-CL")
        )
        formatoFecha.isLenient = false

        val formatoHora = SimpleDateFormat("HH:mm", Locale.ROOT)

        val hoy = formatoFecha.format(Date())
        val horaActual = formatoHora.format(Date())

        // Validar fecha
        if (datos.fecha.isBlank()) {
            errorFecha = "La fecha es obligatoria"

        } else if (!fechaValida(datos.fecha)) {
            errorFecha = "Ingrese una fecha válida (DD/MM/AAAA)"

        } else {
            val fechaReserva = formatoFecha.parse(datos.fecha)
            val fechaHoy = formatoFecha.parse(
                formatoFecha.format(Date())
            )

            if (fechaReserva != null && fechaHoy != null) {
                if (fechaReserva.before(fechaHoy)) {
                    errorFecha = "No puede reservar en una fecha pasada"
                }
            }
        }

        // Validar hora de inicio
        if (datos.horaInicio.isBlank()) {
            errorHoraInicio = "La hora de inicio es obligatoria"
        } else if (!horaValida(datos.horaInicio)) {
            errorHoraInicio = "Ingrese una hora válida (HH:mm)"
        }

        // Validar hora de término
        if (datos.horaFin.isBlank()) {
            errorHoraFin = "La hora de término es obligatoria"
        } else if (!horaValida(datos.horaFin)) {
            errorHoraFin = "Ingrese una hora válida (HH:mm)"
        }

        // No permitir horas pasadas si la reserva es para hoy
        if (errorFecha == null && errorHoraInicio == null) {
            if (datos.fecha == hoy && datos.horaInicio <= horaActual) {
                errorHoraInicio = "La hora de inicio debe ser futura"
            }
        }

        // Comprobar que la hora de término sea posterior
        if (errorFecha == null &&
            errorHoraInicio == null &&
            errorHoraFin == null) {

            if (!horarioPermitido(
                    datos.fecha,
                    datos.horaInicio,
                    datos.horaFin
                )) {
                errorHoraFin = "El horario no está permitido para ese día"
            }
        }

        // Guardar todos los errores
        _errores.value = ReservaErrores(
            fecha = errorFecha,
            horaInicio = errorHoraInicio,
            horaFin = errorHoraFin
        )

        // Resultado de la validación
        if (errorFecha == null &&
            errorHoraInicio == null &&
            errorHoraFin == null) {
            return true
        }

        return false
    }

    private fun fechaValida(fecha: String): Boolean {

        val patron = Regex("""\d{2}/\d{2}/\d{4}""")

        if (!patron.matches(fecha)) {
            return false
        }

        val formato = SimpleDateFormat(
            "dd/MM/yyyy",
            Locale.forLanguageTag("es-CL")
        )

        formato.isLenient = false

        try {
            formato.parse(fecha)
            return true
        } catch (e: ParseException) {
            return false
        }
    }

    private fun horaValida(hora: String): Boolean {

        val patron = Regex("""\d{2}:\d{2}""")

        if (!patron.matches(hora)) {
            return false
        }

        val partes = hora.split(":")

        val horas = partes[0].toInt()
        val minutos = partes[1].toInt()

            if((horas !in 0..23) ||(minutos !in 0  .. 59)){
                return false
            }
            return true

    }

    private fun esDiaExtendido(fecha: String): Boolean {

        val formato = SimpleDateFormat(
            "dd/MM/yyyy",
            Locale.forLanguageTag("es-CL")
        )

        formato.isLenient = false

        val fechaConvertida = try {
            formato.parse(fecha)
        } catch (e: ParseException) {
            null
        }

        if (fechaConvertida == null) {
            return false
        }

        val calendario = Calendar.getInstance()
        calendario.time = fechaConvertida

        val dia = calendario.get(Calendar.DAY_OF_WEEK)

        return dia == Calendar.FRIDAY || dia == Calendar.SATURDAY


    }
    private fun horaAMinutos(hora: String): Int {

        val partes = hora.split(":")

        val horas = partes[0].toInt()
        val minutos = partes[1].toInt()
        val totalMinutos = horas * 60 + minutos
        return totalMinutos
    }

    private fun horarioPermitido(
        fecha: String,
        horaInicio: String,
        horaFin: String
    ): Boolean {

        val inicio = horaAMinutos(horaInicio)
        var fin = horaAMinutos(horaFin)

        val apertura = 8 * 60

        if (inicio < apertura) {
            return false
        }

        if (esDiaExtendido(fecha)) {

            // Viernes y sábado: se permite terminar hasta las
            // 02:00 del día siguiente.
            if (fin <= 2 * 60) {
                fin += 24 * 60
            }

            if (fin <= inicio) {
                return false
            }

            if (fin > 26 * 60) {
                return false
            }

            return true
        }

        // Domingo a jueves: máximo hasta las 22:00.
        if (fin <= inicio) {
            return false
        }

        if (fin > 22 * 60) {
            return false
        }

        return true
    }
}