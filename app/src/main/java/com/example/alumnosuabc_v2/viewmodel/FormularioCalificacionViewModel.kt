package com.example.alumnosuabc_v2.viewmodel

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.alumnosuabc_v2.data.AppDatabase
import com.example.alumnosuabc_v2.data.entity.Calificacion
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado observable del formulario para registrar o editar una calificación.
 */
data class FormularioCalificacionUiState(
    val esEdicion: Boolean = false,
    val alumnoNombre: String = "",
    val materiaNombre: String = "",
    val valorTexto: String = "",
    val mensajeError: String? = null,
    val guardando: Boolean = false,
    val guardadoExitoso: Boolean = false
)

/**
 * ViewModel encargado de validar y guardar calificaciones de una materia para un alumno.
 */
class FormularioCalificacionViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.obtener(application)
    private val _uiState = MutableStateFlow(FormularioCalificacionUiState())
    val uiState: StateFlow<FormularioCalificacionUiState> = _uiState.asStateFlow()

    private var alumnoIdActual: Long = -1L
    private var materiaIdActual: Long = -1L
    private var calificacionIdExistente: Long? = null
    private var inicializado = false

    fun inicializar(alumnoId: Long, materiaId: Long) {
        if (inicializado && alumnoIdActual == alumnoId && materiaIdActual == materiaId) return
        inicializado = true
        alumnoIdActual = alumnoId
        materiaIdActual = materiaId
        calificacionIdExistente = null

        viewModelScope.launch {
            try {
                val alumno = db.alumnoDao().buscarPorId(alumnoId)
                val materia = db.materiaDao().buscarPorId(materiaId)

                if (alumno == null) {
                    _uiState.update {
                        it.copy(
                            materiaNombre = materia?.nombre.orEmpty(),
                            mensajeError = "No se encontró el alumno seleccionado."
                        )
                    }
                    return@launch
                }

                if (materia == null) {
                    _uiState.update {
                        it.copy(
                            alumnoNombre = alumno.nombre,
                            mensajeError = "No se encontró la materia seleccionada."
                        )
                    }
                    return@launch
                }

                if (materia.carreraId != alumno.carreraId) {
                    _uiState.update {
                        it.copy(
                            alumnoNombre = alumno.nombre,
                            materiaNombre = materia.nombre,
                            mensajeError = "La materia no pertenece a la carrera del alumno."
                        )
                    }
                    return@launch
                }

                val calificacion = db.calificacionDao().buscarPorAlumnoYMateria(alumnoId, materiaId)
                calificacionIdExistente = calificacion?.id

                _uiState.update {
                    it.copy(
                        esEdicion = calificacion != null,
                        alumnoNombre = alumno.nombre,
                        materiaNombre = materia.nombre,
                        valorTexto = calificacion?.valor?.toString().orEmpty(),
                        mensajeError = null
                    )
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(mensajeError = "Error al cargar los datos de la calificación.")
                }
            }
        }
    }

    fun onValorChange(nuevoValor: String) {
        _uiState.update { it.copy(valorTexto = nuevoValor, mensajeError = null) }
    }

    fun limpiarError() {
        _uiState.update { it.copy(mensajeError = null) }
    }

    fun guardar() {
        val estadoActual = _uiState.value
        if (estadoActual.guardando) return

        // Se marca guardando de forma síncrona antes de lanzar la corrutina para evitar doble envío.
        _uiState.update { it.copy(guardando = true, mensajeError = null) }
        val valorLimpio = estadoActual.valorTexto.trim()

        viewModelScope.launch {
            try {
                val alumno = db.alumnoDao().buscarPorId(alumnoIdActual)
                if (alumno == null) {
                    _uiState.update {
                        it.copy(mensajeError = "El alumno seleccionado ya no existe en la base de datos.")
                    }
                    return@launch
                }

                val materia = db.materiaDao().buscarPorId(materiaIdActual)
                if (materia == null || materia.carreraId != alumno.carreraId) {
                    _uiState.update {
                        it.copy(mensajeError = "La materia no existe o no pertenece a la carrera del alumno.")
                    }
                    return@launch
                }

                val valorNumerico = valorLimpio.toDoubleOrNull()
                if (
                    valorLimpio.isEmpty() ||
                    valorNumerico == null ||
                    !valorNumerico.isFinite() ||
                    valorNumerico !in 0.0..100.0
                ) {
                    _uiState.update {
                        it.copy(mensajeError = "Ingresa una calificación numérica válida entre 0.0 y 100.0.")
                    }
                    return@launch
                }

                val existente = db.calificacionDao().buscarPorAlumnoYMateria(alumno.id, materia.id)
                if (estadoActual.esEdicion) {
                    val idOriginal = calificacionIdExistente ?: existente?.id
                    if (idOriginal == null) {
                        _uiState.update {
                            it.copy(mensajeError = "La calificación que intentas editar ya no existe.")
                        }
                        return@launch
                    }

                    db.calificacionDao().actualizar(
                        Calificacion(
                            id = idOriginal,
                            alumnoId = alumno.id,
                            materiaId = materia.id,
                            valor = valorNumerico
                        )
                    )
                } else {
                    if (existente != null) {
                        _uiState.update {
                            it.copy(mensajeError = "Ya existe una calificación registrada para esta materia.")
                        }
                        return@launch
                    }

                    db.calificacionDao().insertar(
                        Calificacion(
                            alumnoId = alumno.id,
                            materiaId = materia.id,
                            valor = valorNumerico
                        )
                    )
                }

                _uiState.update {
                    it.copy(
                        valorTexto = valorLimpio,
                        guardadoExitoso = true,
                        mensajeError = null
                    )
                }
            } catch (_: SQLiteConstraintException) {
                _uiState.update {
                    it.copy(mensajeError = "Ya existe una calificación registrada para esta materia.")
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(mensajeError = "Ocurrió un error al guardar la calificación.")
                }
            } finally {
                _uiState.update { it.copy(guardando = false) }
            }
        }
    }
}
