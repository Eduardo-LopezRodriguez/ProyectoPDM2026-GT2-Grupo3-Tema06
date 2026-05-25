package com.example.gradues.data.model

data class GrupoTgeModel(
    val idGrupoTGE: Int,
    val nombreCurso: String,
    val cicloAcademico: String,
    val cupoMaximo: Int,
    val fechaCreacion: String,
    val fechaFinal: String?,
    val totalSubgrupos: Int = 0
)
