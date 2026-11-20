package com.example.alumnosuabc_v2.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.alumnosuabc_v2.data.entity.Carrera
import kotlinx.coroutines.flow.Flow

/**
 * DAO para consultar y precargar el catálogo de carreras.
 */
@Dao
interface CarreraDao {

    @Query("SELECT * FROM carreras ORDER BY id ASC")
    fun observarTodas(): Flow<List<Carrera>>

    @Query("SELECT * FROM carreras ORDER BY id ASC")
    suspend fun obtenerTodas(): List<Carrera>

    @Query("SELECT * FROM carreras WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Long): Carrera?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarTodas(carreras: List<Carrera>)
}
