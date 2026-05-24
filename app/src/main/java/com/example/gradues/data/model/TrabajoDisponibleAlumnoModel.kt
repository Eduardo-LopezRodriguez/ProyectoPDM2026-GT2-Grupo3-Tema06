package com.example.gradues.data.model

data class TrabajoDisponibleAlumnoModel(
    val idTrabajoGraduacion: Int,
    val nombreTrabajo: String,
    val modalidad: String,
    val cicloAcademico: String,
    val docenteResponsable: String,
    val estadoTrabajo: String,
    val cuposDisponiblesTexto: String,
    val descripcionSecundaria: String
)