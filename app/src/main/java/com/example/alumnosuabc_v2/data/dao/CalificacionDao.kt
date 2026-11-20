package com.example.alumnosuabc_v2.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.alumnosuabc_v2.data.entity.Calificacion
import kotlinx.coroutines.flow.Flow

/**
 * DAO para administrar calificaciones y consultar materias con calificación por alumno.
 */
@Dao
interface CalificacionDao {

    @Query(
        """
        SELECT
            m.id AS materiaId,
            m.nombre AS nombreMateria,
            c.id AS calificacionId,
            c.valor AS valor
        FROM materias m
        LEFT JOIN calificaciones c
            ON c.materiaId = m.id AND c.alumnoId = :alumnoId
        WHERE m.carreraId = :carreraId
        ORDER BY m.id ASC
        """
    )
    fun observarMateriasConCalificacion(
        alumnoId: Long,
        carreraId: Long
    ): Flow<List<MateriaConCalificacion>>

    @Query("SELECT COUNT(*) FROM calificaciones WHERE alumnoId = :alumnoId")
    suspend fun contarPorAlumno(alumnoId: Long): Int

    @Query(
        """
        SELECT * FROM calificaciones
        WHERE alumnoId = :alumnoId AND materiaId = :materiaId
        LIMIT 1
        """
    )
    suspend fun buscarPorAlumnoYMateria(alumnoId: Long, materiaId: Long): Calificacion?

    @Query("SELECT * FROM calificaciones WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Long): Calificacion?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(calificacion: Calificacion): Long

    @Update
    suspend fun actualizar(calificacion: Calificacion): Int

    @Delete
    suspend fun eliminar(calificacion: Calificacion): Int
}
