package com.example.gradues.data.model

data class UsuarioAdminModel(
    val idUsuario: Int,
    val nombreUsuario: String,
    val carnetUsuario: String?,
    val correoUsuario: String?,
    val nombreRol: String,
    val carreraUsuario: String?,
    val estadoUsuario: String
)