package com.example.gestionpsdm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionpsdm.model.ReservaFormularioUiState
import com.example.gestionpsdm.repository.ReservasRoomRepository
import com.example.gestionpsdm.utils.HorariosReserva
import java.util.Date
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class ReservaViewModel(
    private val repository: ReservasRoomRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(ReservaFormularioUiState())
    val estado: StateFlow<ReservaFormularioUiState> = _estado

    private val _guardando = MutableStateFlow(false)
    val guardando: StateFlow<Boolean> = _guardando

    private val _ahora = MutableStateFlow(Date())
    val ahora: StateFlow<Date> = _ahora

    suspend fun observarFecha(idEspacio: Int, fecha: String?) {
        _estado.value = ReservaFormularioUiState(fecha = fecha, cargando = fecha != null)

        val reloj = flow {
            while (true) {
                val instante = Date()
                _ahora.value = instante
                emit(instante)
                delay(60_000L - instante.time % 60_000L)
            }
        }

        if (fecha == null) {
            reloj.collect { }
        } else {
            repository.observarBloques(idEspacio, fecha, reloj).collect { bloques ->
                val actualizado = _estado.value.copy(bloques = bloques, cargando = false)
                _estado.value = if (actualizado.rangoDisponible) {
                    actualizado
                } else {
                    actualizado.copy(seleccion = null)
                }
            }
        }
    }

    fun seleccionarBloque(indice: Int): Boolean {
        val anterior = _estado.value
        val actualizado = anterior.seleccionar(indice)
        _estado.value = actualizado
        return actualizado !== anterior
    }

    fun limpiarSeleccion() {
        _estado.value = _estado.value.copy(seleccion = null)
    }

    fun registrarReserva(
        idUsuario: Int,
        idEspacio: Int,
        onResultado: (Boolean, String) -> Unit
    ) {
        val datos = _estado.value
        if (_guardando.value || !datos.rangoDisponible) return
        val rango = datos.seleccion ?: return
        val inicio = datos.bloques[rango.first].inicio
        val fin = datos.bloques[rango.last].fin
        _guardando.value = true

        viewModelScope.launch {
            try {
                val registrado = repository.registrarReserva(
                    idUsuario = idUsuario,
                    idEspacio = idEspacio,
                    fecha = HorariosReserva.fecha(inicio),
                    horaInicio = HorariosReserva.hora(inicio),
                    horaFin = HorariosReserva.hora(fin)
                )
                if (registrado) {
                    limpiarSeleccion()
                    onResultado(true, "Solicitud registrada correctamente")
                } else {
                    onResultado(false, "El horario o el espacio ya no está disponible.")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                onResultado(false, "Error al guardar la reserva")
            } finally {
                _guardando.value = false
            }
        }
    }
}
