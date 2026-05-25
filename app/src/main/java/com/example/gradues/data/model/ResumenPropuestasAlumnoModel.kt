package com.example.gradues.data.model

data class ResumenPropuestasAlumnoModel(
    val total: Int,
    val pendientes: Int,
    val aprobadas: Int,
    val denegadas: Int,
    val borradores: Int,
    val seleccionadas: Int,
    val descartadas: Int,
    val conObservacion: Int
)
