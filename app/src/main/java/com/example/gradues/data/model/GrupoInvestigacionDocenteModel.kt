// GrupoInvestigacionDocenteModel.kt
package com.example.gradues.data.model

data class GrupoInvestigacionDocenteModel(
    val idGrupoTGI: String,
    val idTrabajoGraduacion: String,
    val codigoGrupoTGI: String,
    val nombreTrabajo: String,
    val estadoGrupo: String,
    val totalEstudiantes: Int,
    val totalPropuestas: Int,
    val totalDocumentos: Int
)