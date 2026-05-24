package com.example.gradues.data.model

data class SolicitudAlumnoDetalleModel(
    val idSolicitudModalidad: Int,
    val estadoSolicitud: String,
    val fechaSolicitud: String,
    val modalidad: String,
    val nombreTrabajoPropuesto: String,
    val codigoAgrupacionSolicitud: String,
    val observacionSolicitud: String,
    val nombreTrabajoAsociado: String,
    val nombreCurso: String,
    val nombreEmpresa: String,
    val codigoGrupoTGI: String
)