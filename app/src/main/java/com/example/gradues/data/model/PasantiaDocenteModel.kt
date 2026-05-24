// PasantiaDocenteModel.kt
package com.example.gradues.data.model

data class PasantiaDocenteModel(
    val idProyectoPasantia: String,
    val idTrabajoGraduacion: String,
    val nombreTrabajo: String,
    val idEstudiante: String,
    val carnetEstudiante: String,
    val nombreEstudiante: String,
    val idEmpresa: String,
    val nombreEmpresa: String,
    val nombrePersonero: String,
    val estadoPasantia: String,
    val totalBitacoras: Int,
    val estadoMemoria: String
)