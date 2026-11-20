package com.example.alumnosuabc_v2.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.alumnosuabc_v2.data.dao.AlumnoDao
import com.example.alumnosuabc_v2.data.dao.CalificacionDao
import com.example.alumnosuabc_v2.data.dao.CarreraDao
import com.example.alumnosuabc_v2.data.dao.MateriaDao
import com.example.alumnosuabc_v2.data.entity.Alumno
import com.example.alumnosuabc_v2.data.entity.Calificacion
import com.example.alumnosuabc_v2.data.entity.Carrera
import com.example.alumnosuabc_v2.data.entity.Materia

/**
 * Base de datos local de Room para el sistema de control escolar.
 */
@Database(
    entities = [
        Carrera::class,
        Materia::class,
        Alumno::class,
        Calificacion::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun carreraDao(): CarreraDao

    abstract fun materiaDao(): MateriaDao

    abstract fun alumnoDao(): AlumnoDao

    abstract fun calificacionDao(): CalificacionDao

    companion object {
        private const val NOMBRE_BD = "control_escolar.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Devuelve la instancia única de [AppDatabase] usando el contexto de aplicación.
         */
        fun obtener(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    NOMBRE_BD
                ).build().also { INSTANCE = it }
            }
        }
    }
}
