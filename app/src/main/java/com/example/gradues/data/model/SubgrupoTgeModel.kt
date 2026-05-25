package com.example.gradues.data.model

data class SubgrupoTgeModel(
    val idSubgrupoTGE: Int,
    val idGrupoTGE: Int,
    val idTrabajoGraduacion: Int?,
    val nombreSubgrupo: String,
    val temaAsignado: String?,
    val totalAlumnos: Int = 0,
    val idDocenteResponsable: Int? = null,
    val nombreDocente: String? = null
)