package com.example.alumnosuabc_v2.data.dao

/**
 * Proyección de consulta para mostrar cada materia de la carrera con su calificación opcional.
 */
data class MateriaConCalificacion(
    val materiaId: Long,
    val nombreMateria: String,
    val calificacionId: Long?,
    val valor: Double?
)
