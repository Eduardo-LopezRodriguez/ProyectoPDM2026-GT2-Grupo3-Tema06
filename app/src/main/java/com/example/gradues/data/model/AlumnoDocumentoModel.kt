package com.example.gradues.data.model

data class AlumnoDocumentoModel(
    val idDocumento: Int,
    val idTrabajoGraduacion: Int,
    val tipoDocumento: String,
    val tituloDocumento: String,
    val urlDocumento: String?,
    val estadoDocumento: String,
    val observacionDocumento: String,
    val versionDocumento: Int,
    val fechaSubida: String
)
