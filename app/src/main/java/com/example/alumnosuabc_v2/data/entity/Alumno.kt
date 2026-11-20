package com.example.alumnosuabc_v2.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Representa un alumno inscrito en una carrera dentro del control escolar.
 */
@Entity(
    tableName = "alumnos",
    foreignKeys = [
        ForeignKey(
            entity = Carrera::class,
            parentColumns = ["id"],
            childColumns = ["carreraId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["matricula"], unique = true),
        Index(value = ["carreraId"])
    ]
)
data class Alumno(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val matricula: String,
    val correo: String,
    val semestre: Int,
    val carreraId: Long
)
