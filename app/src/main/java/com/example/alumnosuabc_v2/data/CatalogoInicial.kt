package com.example.alumnosuabc_v2.data

import androidx.room.withTransaction
import com.example.alumnosuabc_v2.data.entity.Carrera
import com.example.alumnosuabc_v2.data.entity.Materia

private val carrerasIniciales = listOf(
    Carrera(id = 1L, nombre = "Ingeniería en Computación"),
    Carrera(id = 2L, nombre = "Ingeniería Eléctrica"),
    Carrera(id = 3L, nombre = "Ingeniería Industrial"),
    Carrera(id = 4L, nombre = "Ingeniería Civil")
)

private val materiasPorCarrera = mapOf(
    1L to listOf(
        "Cálculo Diferencial",
        "Álgebra Superior",
        "Metodología de la Programación",
        "Cálculo Integral",
        "Probabilidad y Estadística",
        "Programación y Métodos Numéricos",
        "Ecuaciones Diferenciales",
        "Electricidad y Magnetismo",
        "Programación Estructurada",
        "Matemáticas Discretas",
        "Circuitos Digitales",
        "Programación Orientada a Objetos",
        "Organización y Arquitectura de Computadoras",
        "Diseño Digital",
        "Algoritmos y Estructuras de Datos",
        "Microcontroladores",
        "Bases de Datos",
        "Redes de Computadoras",
        "Sistemas Embebidos",
        "Ingeniería de Software",
        "Sistemas Operativos",
        "Internet de las Cosas",
        "Programación de Dispositivos Móviles",
        "Proyecto de Carrera"
    ),
    2L to listOf(
        "Cálculo Diferencial",
        "Álgebra Superior",
        "Metodología de la Programación",
        "Cálculo Integral",
        "Mecánica Vectorial",
        "Química",
        "Ecuaciones Diferenciales",
        "Electricidad y Magnetismo",
        "Circuitos",
        "Matemáticas Avanzadas",
        "Circuitos Aplicados",
        "Electrónica Analógica",
        "Mediciones Eléctricas",
        "Máquinas de Inducción",
        "Teoría de Control",
        "Máquinas de C.D. y Síncronas",
        "Estudio de Cortocircuito",
        "Sistemas de Distribución",
        "Subestaciones Eléctricas",
        "Instalaciones Eléctricas",
        "Sistemas de Potencia",
        "Plantas Eléctricas",
        "Calidad de la Energía",
        "Control de Motores Eléctricos"
    ),
    3L to listOf(
        "Cálculo Diferencial",
        "Álgebra Superior",
        "Introducción a la Ingeniería",
        "Cálculo Integral",
        "Probabilidad y Estadística",
        "Mecánica Vectorial",
        "Ecuaciones Diferenciales",
        "Metrología",
        "Estadística Industrial",
        "Tópicos de Calidad",
        "Control de Calidad y Confiabilidad",
        "Procesos de Manufactura",
        "Investigación de Operaciones 1",
        "Diseño de Experimentos",
        "Ergonomía",
        "Planeación y Control de la Producción",
        "Investigación de Operaciones 2",
        "Tópicos de Mejora Continua",
        "Administración de Operaciones",
        "Simulación de Procesos",
        "Manufactura Avanzada",
        "Formulación y Evaluación de Proyectos",
        "Cadena de Suministro y Logística",
        "Sistemas de Gestión"
    ),
    4L to listOf(
        "Cálculo Diferencial",
        "Álgebra Superior",
        "Introducción a la Ingeniería",
        "Cálculo Integral",
        "Mecánica Vectorial",
        "Probabilidad y Estadística",
        "Estructuras Isostáticas",
        "Geología Aplicada",
        "Topografía",
        "Procesos de Construcción",
        "Hidráulica I",
        "Resistencia de Materiales",
        "Costos y Presupuestos",
        "Hidráulica II",
        "Análisis Estructural",
        "Sistemas de Abastecimiento de Agua Potable",
        "Diseño Estructural de Concreto y Mampostería",
        "Pavimentos",
        "Planeación y Control de Obra",
        "Diseño de Estructuras de Acero y Ligeras",
        "Cimentaciones",
        "Formulación y Evaluación de Proyectos",
        "Diseño de Obras Hidráulicas",
        "Proyecto Estructural"
    )
)

/**
 * Inserta de forma segura e idempotente el catálogo inicial de 4 carreras y 96 materias.
 * Usa [OnConflictStrategy.IGNORE] en los DAOs para no duplicar ni alterar registros existentes.
 */
suspend fun asegurarCatalogoInicial(db: AppDatabase) {
    val materiasIniciales = materiasPorCarrera.flatMap { (carreraId, nombres) ->
        nombres.map { nombre -> Materia(nombre = nombre, carreraId = carreraId) }
    }

    db.withTransaction {
        db.carreraDao().insertarTodas(carrerasIniciales)
        db.materiaDao().insertarTodas(materiasIniciales)
    }
}
