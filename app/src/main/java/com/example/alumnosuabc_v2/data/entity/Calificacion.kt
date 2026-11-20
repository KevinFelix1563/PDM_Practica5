package com.example.alumnosuabc_v2.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Representa la calificación de un alumno en una materia determinada.
 */
@Entity(
    tableName = "calificaciones",
    foreignKeys = [
        ForeignKey(
            entity = Alumno::class,
            parentColumns = ["id"],
            childColumns = ["alumnoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Materia::class,
            parentColumns = ["id"],
            childColumns = ["materiaId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["alumnoId", "materiaId"], unique = true),
        Index(value = ["alumnoId"]),
        Index(value = ["materiaId"])
    ]
)
data class Calificacion(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val alumnoId: Long,
    val materiaId: Long,
    val valor: Double
)
