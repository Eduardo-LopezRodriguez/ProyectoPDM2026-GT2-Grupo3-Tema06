package com.example.gradues.data.model

data class AlumnoDisponibleModel(
    val idUsuario: Int,
    val nombreCompleto: String,
    val carnet: String,
    val correo: String,
    val asignadoEnEsteSubgrupo: Boolean = false
)
