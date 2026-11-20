package com.example.alumnosuabc_v2.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Representa una carrera de ingeniería del catálogo precargado.
 */
@Entity(
    tableName = "carreras",
    indices = [
        Index(value = ["nombre"], unique = true)
    ]
)
data class Carrera(
    @PrimaryKey val id: Long,
    val nombre: String
)
