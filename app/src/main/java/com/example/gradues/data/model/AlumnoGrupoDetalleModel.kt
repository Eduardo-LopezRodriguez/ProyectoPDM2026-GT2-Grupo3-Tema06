package com.example.gradues.data.model

data class AlumnoGrupoDetalleModel(
    val nombreGrupo: String,
    val codigoGrupo: String,
    val temaTrabajo: String,
    val nombreDocente: String,
    val integrantesTexto: String,
    val cantidadIntegrantes: Int,
    val estadoGeneral: String,
    val notaEtapa1: String,
    val notaEtapa2: String,
    val notaEtapa3: String,
    val notaEtapa4: String,
    val resumenPropuestas: String,
    val descripcionTesis: String,
    val estadoTesis: String,
    val ultimaVersionTesis: String
)