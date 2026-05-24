package com.example.gradues.data.model

data class DashboardAlumnoModel(
    val nombreCompleto: String,
    val correoUsuario: String,
    val carnetUsuario: String,

    val tipoDashboard: String, // INVESTIGACION, ESPECIALIZACION, PASANTIA, SIN_TRABAJO

    val tituloSeccionPrincipal: String,
    val tituloTarjetaPrincipal: String,
    val subtituloTarjetaPrincipal: String,
    val textoBotonPrincipal: String,

    val mostrarBloquePropuestas: Boolean,
    val tituloBloqueSecundario: String,
    val descripcionBloqueSecundario: String,
    val estadoBloqueSecundario: String,
    val textoBotonSecundario: String,

    val mostrarBloqueNotas: Boolean,
    val notaEtapa1: String,
    val notaEtapa2: String,
    val notaEtapa3: String,
    val notaEtapa4: String,

    val textoBotonAccionInferior: String,
    val tieneTrabajoAsignado: Boolean,

    val estadoSolicitudTexto: String = "Estado: Sin solicitud",
    val detalleSolicitudTexto: String = "Todavía no has realizado una solicitud de modalidad."
)