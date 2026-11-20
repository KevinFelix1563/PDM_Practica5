package com.example.alumnosuabc_v2.viewmodel

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.alumnosuabc_v2.data.AppDatabase
import com.example.alumnosuabc_v2.data.asegurarCatalogoInicial
import com.example.alumnosuabc_v2.data.entity.Alumno
import com.example.alumnosuabc_v2.data.entity.Carrera
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado observable del formulario para registrar o editar un alumno.
 */
data class FormularioAlumnoUiState(
    val esEdicion: Boolean = false,
    val alumnoId: Long = -1L,
    val nombre: String = "",
    val matricula: String = "",
    val correo: String = "",
    val semestre: String = "",
    val carreraIdSeleccionada: Long? = null,
    val carrerasDisponibles: List<Carrera> = emptyList(),
    val puedeCambiarCarrera: Boolean = true,
    val avisoCambioCarrera: String? = null,
    val mensajeError: String? = null,
    val guardando: Boolean = false,
    val guardadoExitoso: Boolean = false
)

/**
 * ViewModel para el alta y edición de alumnos, incluyendo validaciones y bloqueo
 * de cambio de carrera cuando el alumno ya tiene calificaciones registradas.
 */
class FormularioAlumnoViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.obtener(application)
    private val _uiState = MutableStateFlow(FormularioAlumnoUiState())
    val uiState: StateFlow<FormularioAlumnoUiState> = _uiState.asStateFlow()

    private var inicializado = false
    private var idInicializado: Long? = null

    fun inicializar(alumnoId: Long) {
        if (inicializado && idInicializado == alumnoId) return
        inicializado = true
        idInicializado = alumnoId

        viewModelScope.launch {
            try {
                asegurarCatalogoInicial(db)
                val carreras = db.carreraDao().obtenerTodas()

                if (alumnoId > 0L) {
                    val alumno = db.alumnoDao().buscarPorId(alumnoId)
                    if (alumno == null) {
                        _uiState.update {
                            it.copy(
                                carrerasDisponibles = carreras,
                                mensajeError = "No se encontró el alumno seleccionado."
                            )
                        }
                        return@launch
                    }

                    val totalCalificaciones = db.calificacionDao().contarPorAlumno(alumnoId)
                    val puedeCambiar = totalCalificaciones == 0
                    val aviso = if (puedeCambiar) {
                        null
                    } else {
                        "No puedes cambiar de carrera porque este alumno ya tiene calificaciones registradas."
                    }

                    _uiState.update {
                        it.copy(
                            esEdicion = true,
                            alumnoId = alumno.id,
                            nombre = alumno.nombre,
                            matricula = alumno.matricula,
                            correo = alumno.correo,
                            semestre = alumno.semestre.toString(),
                            carreraIdSeleccionada = alumno.carreraId,
                            carrerasDisponibles = carreras,
                            puedeCambiarCarrera = puedeCambiar,
                            avisoCambioCarrera = aviso,
                            mensajeError = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            esEdicion = false,
                            alumnoId = -1L,
                            carrerasDisponibles = carreras,
                            puedeCambiarCarrera = true,
                            avisoCambioCarrera = null,
                            mensajeError = null
                        )
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(mensajeError = "Error al cargar los datos del formulario.")
                }
            }
        }
    }

    fun onNombreChange(valor: String) {
        _uiState.update { it.copy(nombre = valor, mensajeError = null) }
    }

    fun onMatriculaChange(valor: String) {
        _uiState.update { it.copy(matricula = valor, mensajeError = null) }
    }

    fun onCorreoChange(valor: String) {
        _uiState.update { it.copy(correo = valor, mensajeError = null) }
    }

    fun onSemestreChange(valor: String) {
        _uiState.update { it.copy(semestre = valor, mensajeError = null) }
    }

    fun onCarreraChange(carreraId: Long) {
        if (!_uiState.value.puedeCambiarCarrera) return
        _uiState.update { it.copy(carreraIdSeleccionada = carreraId, mensajeError = null) }
    }

    fun limpiarError() {
        _uiState.update { it.copy(mensajeError = null) }
    }

    fun guardar() {
        val estadoActual = _uiState.value
        if (estadoActual.guardando) return

        // Se marca guardando de forma síncrona antes de lanzar la corrutina para evitar doble envío.
        _uiState.update { it.copy(guardando = true, mensajeError = null) }

        val nombreLimpio = estadoActual.nombre.trim()
        val matriculaLimpia = estadoActual.matricula.trim()
        val correoLimpio = estadoActual.correo.trim()
        val semestreTexto = estadoActual.semestre.trim()

        viewModelScope.launch {
            try {
                if (
                    nombreLimpio.isEmpty() ||
                    matriculaLimpia.isEmpty() ||
                    correoLimpio.isEmpty() ||
                    semestreTexto.isEmpty()
                ) {
                    _uiState.update {
                        it.copy(mensajeError = "Todos los campos obligatorios deben estar completos.")
                    }
                    return@launch
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(correoLimpio).matches()) {
                    _uiState.update {
                        it.copy(mensajeError = "Ingresa un correo electrónico válido.")
                    }
                    return@launch
                }

                val semestreNumero = semestreTexto.toIntOrNull()
                if (semestreNumero == null || semestreNumero <= 0) {
                    _uiState.update {
                        it.copy(mensajeError = "El semestre debe ser un número entero mayor que cero.")
                    }
                    return@launch
                }

                val carreraId = estadoActual.carreraIdSeleccionada
                if (carreraId == null || db.carreraDao().buscarPorId(carreraId) == null) {
                    _uiState.update {
                        it.copy(mensajeError = "Selecciona una carrera válida.")
                    }
                    return@launch
                }

                if (estadoActual.esEdicion) {
                    val alumnoActual = db.alumnoDao().buscarPorId(estadoActual.alumnoId)
                    if (alumnoActual == null) {
                        _uiState.update {
                            it.copy(mensajeError = "El alumno que intentas editar ya no existe.")
                        }
                        return@launch
                    }

                    val totalCalificaciones = db.calificacionDao().contarPorAlumno(alumnoActual.id)
                    if (totalCalificaciones > 0 && carreraId != alumnoActual.carreraId) {
                        _uiState.update {
                            it.copy(
                                carreraIdSeleccionada = alumnoActual.carreraId,
                                puedeCambiarCarrera = false,
                                avisoCambioCarrera = "No puedes cambiar de carrera porque este alumno ya tiene calificaciones registradas.",
                                mensajeError = "No es posible cambiar la carrera porque el alumno ya tiene calificaciones registradas."
                            )
                        }
                        return@launch
                    }
                }

                val existente = db.alumnoDao().buscarPorMatricula(matriculaLimpia)
                val matriculaDuplicada = existente != null &&
                    (!estadoActual.esEdicion || existente.id != estadoActual.alumnoId)
                if (matriculaDuplicada) {
                    _uiState.update {
                        it.copy(mensajeError = "Ya existe un alumno registrado con esa matrícula.")
                    }
                    return@launch
                }

                val alumno = Alumno(
                    id = if (estadoActual.esEdicion) estadoActual.alumnoId else 0L,
                    nombre = nombreLimpio,
                    matricula = matriculaLimpia,
                    correo = correoLimpio,
                    semestre = semestreNumero,
                    carreraId = carreraId
                )

                if (estadoActual.esEdicion) {
                    db.alumnoDao().actualizar(alumno)
                } else {
                    db.alumnoDao().insertar(alumno)
                }

                _uiState.update {
                    it.copy(
                        nombre = nombreLimpio,
                        matricula = matriculaLimpia,
                        correo = correoLimpio,
                        semestre = semestreTexto,
                        guardadoExitoso = true,
                        mensajeError = null
                    )
                }
            } catch (_: SQLiteConstraintException) {
                _uiState.update {
                    it.copy(mensajeError = "No se pudo guardar: la matrícula ya está registrada o los datos no son válidos.")
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(mensajeError = "Ocurrió un error al guardar el alumno.")
                }
            } finally {
                _uiState.update { it.copy(guardando = false) }
            }
        }
    }
}
