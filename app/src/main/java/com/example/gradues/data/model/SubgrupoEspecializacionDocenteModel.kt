// SubgrupoEspecializacionDocenteModel.kt
package com.example.gradues.data.model

data class SubgrupoEspecializacionDocenteModel(
    val idSubgrupoTGE: String,
    val idTrabajoGraduacion: String,
    val nombreSubgrupo: String,
    val temaAsignado: String,
    val estadoSubgrupo: String,
    val totalEstudiantes: Int,
    val promedioGeneralSubgrupo: String
)