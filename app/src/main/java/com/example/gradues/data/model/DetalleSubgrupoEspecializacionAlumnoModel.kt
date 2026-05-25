package com.example.gradues.data.model

data class DetalleSubgrupoEspecializacionAlumnoModel(
    val idTrabajoGraduacion: Int,
    val idAlumnoTrabajo: Int,
    val nombreSubgrupo: String,
    val nombreCurso: String,
    val temaAsignado: String,
    val director: IntegranteSubgrupoAlumnoModel?,
    val integrantes: List<IntegranteSubgrupoAlumnoModel>,
    val jurados: List<JuradoEspecializacionAlumnoModel>,
    val resumenPropuestas: ResumenPropuestasAlumnoModel,
    val tesina: DocumentoTesinaAlumnoModel?,
    val notas: List<NotaAlumnoModel>,
    val promedioAlumno: Double?,
    val promedioGrupo: Double?
)
