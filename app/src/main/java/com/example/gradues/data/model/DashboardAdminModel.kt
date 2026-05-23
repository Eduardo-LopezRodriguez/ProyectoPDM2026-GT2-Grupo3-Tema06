package com.example.gradues.data.model

data class DashboardAdminModel(
    val idUsuario: String,
    val nombreCompleto: String,
    val correo: String,
    val totalUsuarios: Int,
    val totalAlumnos: Int,
    val totalDocentes: Int,
    val totalTrabajosActivos: Int,
    val solicitudesPendientes: Int,
    val solicitudesAprobadas: Int
)