package com.example.alumnosuabc_v2.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Representa una materia perteneciente a una carrera específica.
 */
@Entity(
    tableName = "materias",
    foreignKeys = [
        ForeignKey(
            entity = Carrera::class,
            parentColumns = ["id"],
            childColumns = ["carreraId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["carreraId"]),
        Index(value = ["carreraId", "nombre"], unique = true)
    ]
)
data class Materia(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val carreraId: Long
)
