package com.example.gradues.data.model

data class DashboardDocenteModel(
    val idUsuario: String,
    val nombreCompleto: String,
    val correo: String,
    val totalInvestigacion: Int,
    val totalEspecializacion: Int,
    val totalPasantia: Int,
    val documentosRevision: Int,
    val bitacorasPendientes: Int
)