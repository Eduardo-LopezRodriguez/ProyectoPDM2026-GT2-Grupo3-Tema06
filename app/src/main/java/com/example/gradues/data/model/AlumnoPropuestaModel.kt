package com.example.gradues.data.model

data class AlumnoPropuestaModel(
    val idPropuesta: Int,
    val tituloPropuesta: String,
    val descripcionPropuesta: String,
    val estadoPropuesta: String,
    val observacionPropuesta: String,
    val urlArchivo: String?,
    val fechaRegistro: String
)
