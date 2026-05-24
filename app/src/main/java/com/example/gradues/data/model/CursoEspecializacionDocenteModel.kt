// CursoEspecializacionDocenteModel.kt
package com.example.gradues.data.model

data class CursoEspecializacionDocenteModel(
    val idGrupoTGE: String,
    val nombreCurso: String,
    val ciclo: String,
    val totalSubgrupos: Int,
    val totalEstudiantes: Int
)