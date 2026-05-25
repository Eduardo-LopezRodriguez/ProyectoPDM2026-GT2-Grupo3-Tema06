package com.example.gradues.data.model

data class DocumentoTesinaAlumnoModel(
    val tituloDocumento: String,
    val estadoDocumento: String,
    val versionDocumento: Int?,
    val observacionDocumento: String?,
    val fechaSubida: String?
)
