package com.example.gradues.data.model

data class UsuarioAdminDetalleModel(
    val idUsuario: Int,
    val idRol: Int,
    val nombreRol: String,
    val primerNombreUsuario: String,
    val segundoNombreUsuario: String?,
    val primerApellidoUsuario: String,
    val segundoApellidoUsuario: String?,
    val correoUsuario: String,
    val carnetUsuario: String?,
    val duiUsuario: String?,
    val carreraUsuario: String?,
    val estadoUsuario: String
)