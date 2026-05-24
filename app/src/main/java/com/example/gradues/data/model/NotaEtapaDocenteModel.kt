// NotaEtapaDocenteModel.kt
package com.example.gradues.data.model

data class NotaEtapaDocenteModel(
    val idNotaEtapa: String?,
    val idTrabajoGraduacion: String,
    val idUsuario: String,
    val nombreEtapa: String,
    val nota: Double?,
    val observacion: String?,
    val fechaRegistro: String
)