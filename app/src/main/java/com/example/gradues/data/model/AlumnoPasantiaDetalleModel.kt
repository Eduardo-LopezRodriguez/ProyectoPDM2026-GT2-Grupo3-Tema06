package com.example.gradues.data.model

data class AlumnoPasantiaDetalleModel(
    val idProyectoPasantia: Int,
    val idTrabajoGraduacion: Int,
    val nombreAlumno: String,
    val carnetAlumno: String,
    val nombreDocente: String,
    val nombreTrabajo: String,
    val estadoTrabajo: String,
    val cicloAcademico: String,
    val fechaInicioPasantia: String,
    val fechaFinalPasantia: String,
    val estadoPasantia: String,
    val nombreEmpresa: String,
    val rubroEmpresa: String,
    val nombrePersonero: String,
    val cargoPersonero: String
)