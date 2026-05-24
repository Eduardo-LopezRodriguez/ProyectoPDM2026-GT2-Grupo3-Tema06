package com.example.gradues.data.model

data class BitacoraPendienteDocenteModel(
    val idBitacora: String,
    val idProyectoPasantia: String,
    val idTrabajoGraduacion: String,
    val tituloActividad: String,
    val descripcionActividad: String,
    val fechaActividad: String,
    val totalHorasTrabajadas: Int,
    val estadoBitacora: String,
    val observacionBitacora: String?,
    val nombreEstudiante: String,
    val carnetEstudiante: String,
    val nombreTrabajo: String,
    val nombreEmpresa: String
)