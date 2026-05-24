// MemoriaDocenteModel.kt
package com.example.gradues.data.model

data class MemoriaDocenteModel(
    val idMemoriaResumen: String,
    val tituloMemoria: String,
    val urlDocumento: String?,
    val estadoMemoria: String,
    val observacionMemoria: String?
)