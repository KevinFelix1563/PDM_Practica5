package com.example.alumnosuabc_v2.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.alumnosuabc_v2.data.AppDatabase
import com.example.alumnosuabc_v2.data.asegurarCatalogoInicial
import com.example.alumnosuabc_v2.data.dao.AlumnoConCarrera
import com.example.alumnosuabc_v2.data.entity.Carrera
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Estado observable de la pantalla principal de alumnos.
 */
data class AlumnosUiState(
    val carreras: List<Carrera> = emptyList(),
    val carreraSeleccionadaId: Long? = null,
    val alumnos: List<AlumnoConCarrera> = emptyList(),
    val mensajeError: String? = null
)

/**
 * ViewModel de la pantalla principal encargado de precargar el catálogo,
 * filtrar alumnos por carrera y eliminar registros de alumnos.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AlumnosViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.obtener(application)
    private val carreraSeleccionadaId = MutableStateFlow<Long?>(null)
    private val mensajeError = MutableStateFlow<String?>(null)

    private val alumnosFiltrados = carreraSeleccionadaId.flatMapLatest { carreraId ->
        if (carreraId == null) {
            db.alumnoDao().observarTodosConCarrera()
        } else {
            db.alumnoDao().observarPorCarrera(carreraId)
        }
    }

    val uiState: StateFlow<AlumnosUiState> = combine(
        db.carreraDao().observarTodas(),
        carreraSeleccionadaId,
        alumnosFiltrados,
        mensajeError
    ) { carreras, seleccionId, alumnos, error ->
        AlumnosUiState(
            carreras = carreras,
            carreraSeleccionadaId = seleccionId,
            alumnos = alumnos,
            mensajeError = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = AlumnosUiState()
    )

    init {
        viewModelScope.launch {
            try {
                asegurarCatalogoInicial(db)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                mensajeError.value = "Error al inicializar el catálogo de carreras y materias."
            }
        }
    }

    fun seleccionarCarrera(carreraId: Long?) {
        carreraSeleccionadaId.value = carreraId
    }

    fun eliminarAlumno(alumno: AlumnoConCarrera) {
        viewModelScope.launch {
            try {
                val registro = db.alumnoDao().buscarPorId(alumno.id) ?: return@launch
                db.alumnoDao().eliminar(registro)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                mensajeError.value = "No se pudo eliminar al alumno."
            }
        }
    }

    fun limpiarError() {
        mensajeError.value = null
    }
}
