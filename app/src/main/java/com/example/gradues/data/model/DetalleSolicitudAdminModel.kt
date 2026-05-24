package com.example.gradues.data.model

data class DetalleSolicitudAdminModel(
    val idReferenciaSolicitud: Int,
    val idModalidad: Int,
    val idTrabajoGraduacion: Int?,
    val codigoAgrupacionSolicitud: String,
    val modalidad: String,
    val nombreTrabajoPropuesto: String,
    val cursoSolicitado: String?,
    val empresaSolicitada: String?,
    val estadoSolicitud: String,
    val fechaSolicitud: String,
    val observacionSolicitud: String?,
    val totalSolicitantes: Int,
    val solicitantes: String
)