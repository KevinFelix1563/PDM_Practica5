package com.example.alumnosuabc_v2.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.alumnosuabc_v2.data.AppDatabase
import com.example.alumnosuabc_v2.data.dao.AlumnoConCarrera
import com.example.alumnosuabc_v2.data.dao.MateriaConCalificacion
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Estado observable del detalle del alumno, sus materias y su promedio general.
 */
data class DetalleAlumnoUiState(
    val cargando: Boolean = true,
    val alumno: AlumnoConCarrera? = null,
    val materias: List<MateriaConCalificacion> = emptyList(),
    val promedioTexto: String = "Sin calificaciones",
    val mensajeError: String? = null
)

/**
 * ViewModel para observar la información de un alumno, las 24 materias de su carrera
 * con sus calificaciones y el cálculo automático del promedio.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DetalleAlumnoViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.obtener(application)
    private val alumnoIdFlow = MutableStateFlow<Long?>(null)
    private val mensajeError = MutableStateFlow<String?>(null)

    private val detalleFlow = alumnoIdFlow.flatMapLatest { alumnoId ->
        when {
            alumnoId == null -> flowOf(DetalleAlumnoUiState(cargando = true))
            alumnoId <= 0L -> flowOf(
                DetalleAlumnoUiState(
                    cargando = false,
                    mensajeError = "Identificador de alumno no válido."
                )
            )
            else -> db.alumnoDao().observarPorIdConCarrera(alumnoId).flatMapLatest { alumno ->
                if (alumno == null) {
                    flowOf(
                        DetalleAlumnoUiState(
                            cargando = false,
                            alumno = null,
                            materias = emptyList(),
                            promedioTexto = "Sin calificaciones"
                        )
                    )
                } else {
                    db.calificacionDao()
                        .observarMateriasConCalificacion(alumno.id, alumno.carreraId)
                        .map { materias ->
                            DetalleAlumnoUiState(
                                cargando = false,
                                alumno = alumno,
                                materias = materias,
                                promedioTexto = calcularPromedioTexto(materias)
                            )
                        }
                }
            }
        }
    }

    val uiState: StateFlow<DetalleAlumnoUiState> = combine(
        detalleFlow,
        mensajeError
    ) { detalle, error ->
        detalle.copy(mensajeError = error ?: detalle.mensajeError)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = DetalleAlumnoUiState()
    )

    fun cargarAlumno(alumnoId: Long) {
        if (alumnoIdFlow.value != alumnoId) {
            alumnoIdFlow.value = alumnoId
        }
    }

    fun eliminarCalificacion(calificacionId: Long) {
        viewModelScope.launch {
            try {
                val calificacion = db.calificacionDao().buscarPorId(calificacionId) ?: return@launch
                db.calificacionDao().eliminar(calificacion)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                mensajeError.value = "No se pudo eliminar la calificación."
            }
        }
    }

    fun limpiarError() {
        mensajeError.value = null
    }

    private fun calcularPromedioTexto(materias: List<MateriaConCalificacion>): String {
        val registradas = materias.mapNotNull { it.valor }
        return if (registradas.isEmpty()) {
            "Sin calificaciones"
        } else {
            String.format(Locale.US, "%.2f", registradas.average())
        }
    }
}
