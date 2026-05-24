// DocumentoInvestigacionDocenteModel.kt
package com.example.gradues.data.model

data class DocumentoInvestigacionDocenteModel(
    val idDocumento: String,
    val nombreDocumento: String,
    val tipoDocumento: String,
    val versionDocumento: Int,
    val urlDocumento: String?,
    val estadoDocumento: String,
    val observacionDocumento: String?,
    val fechaCarga: String
)