package com.example.gradues.data.model

data class TrabajoGraduacionAdminModel(
    val idTrabajoGraduacion: Int,
    val nombreTrabajo: String,
    val modalidad: String,
    val docenteResponsable: String,
    val cicloAcademico: String,
    val estadoTrabajo: String,
    val fechaInicioTrabajo: String,
    val fechaFinalTrabajo: String?,
    val detalleModalidad: String,
    val totalIntegrantes: Int,
    val integrantes: String
)