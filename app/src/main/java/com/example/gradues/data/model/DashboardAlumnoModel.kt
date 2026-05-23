package com.example.gradues.data.model

data class DashboardAlumnoModel(
    val nombreCompleto: String,
    val carnet: String,
    val modalidadActual: String,
    val estadoSolicitud: String,
    val nombreTrabajo: String,
    val codigoAgrupacion: String,
    val totalDocumentos: Int,
    val totalPropuestas: Int,
    val totalNotasRegistradas: Int,
    val promedioNotas: Double
)