package com.example.alumnosuabc_v2.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.alumnosuabc_v2.data.entity.Materia

/**
 * DAO para consultar y precargar las materias por carrera.
 */
@Dao
interface MateriaDao {

    @Query("SELECT * FROM materias WHERE carreraId = :carreraId ORDER BY id ASC")
    suspend fun obtenerPorCarrera(carreraId: Long): List<Materia>

    @Query("SELECT * FROM materias WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Long): Materia?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarTodas(materias: List<Materia>)
}
