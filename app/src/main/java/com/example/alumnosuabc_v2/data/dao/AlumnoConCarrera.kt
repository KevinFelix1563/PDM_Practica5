package com.example.alumnosuabc_v2.data.dao

/**
 * Proyección de consulta para mostrar un alumno junto con el nombre de su carrera.
 */
data class AlumnoConCarrera(
    val id: Long,
    val nombre: String,
    val matricula: String,
    val correo: String,
    val semestre: Int,
    val carreraId: Long,
    val nombreCarrera: String
)
