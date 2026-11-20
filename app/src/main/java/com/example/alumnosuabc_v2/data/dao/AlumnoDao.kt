package com.example.alumnosuabc_v2.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.alumnosuabc_v2.data.entity.Alumno
import kotlinx.coroutines.flow.Flow

/**
 * DAO para administrar alumnos y consultar su información junto con su carrera.
 */
@Dao
interface AlumnoDao {

    @Query(
        """
        SELECT
            a.id AS id,
            a.nombre AS nombre,
            a.matricula AS matricula,
            a.correo AS correo,
            a.semestre AS semestre,
            a.carreraId AS carreraId,
            c.nombre AS nombreCarrera
        FROM alumnos a
        INNER JOIN carreras c ON a.carreraId = c.id
        ORDER BY a.nombre ASC, a.id ASC
        """
    )
    fun observarTodosConCarrera(): Flow<List<AlumnoConCarrera>>

    @Query(
        """
        SELECT
            a.id AS id,
            a.nombre AS nombre,
            a.matricula AS matricula,
            a.correo AS correo,
            a.semestre AS semestre,
            a.carreraId AS carreraId,
            c.nombre AS nombreCarrera
        FROM alumnos a
        INNER JOIN carreras c ON a.carreraId = c.id
        WHERE a.carreraId = :carreraId
        ORDER BY a.nombre ASC, a.id ASC
        """
    )
    fun observarPorCarrera(carreraId: Long): Flow<List<AlumnoConCarrera>>

    @Query(
        """
        SELECT
            a.id AS id,
            a.nombre AS nombre,
            a.matricula AS matricula,
            a.correo AS correo,
            a.semestre AS semestre,
            a.carreraId AS carreraId,
            c.nombre AS nombreCarrera
        FROM alumnos a
        INNER JOIN carreras c ON a.carreraId = c.id
        WHERE a.id = :id
        LIMIT 1
        """
    )
    fun observarPorIdConCarrera(id: Long): Flow<AlumnoConCarrera?>

    @Query("SELECT * FROM alumnos WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Long): Alumno?

    @Query("SELECT * FROM alumnos WHERE matricula = :matricula LIMIT 1")
    suspend fun buscarPorMatricula(matricula: String): Alumno?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(alumno: Alumno): Long

    @Update
    suspend fun actualizar(alumno: Alumno): Int

    @Delete
    suspend fun eliminar(alumno: Alumno): Int
}
