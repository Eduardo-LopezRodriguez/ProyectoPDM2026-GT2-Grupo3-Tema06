// PropuestaPerfilDocenteModel.kt
package com.example.gradues.data.model

data class PropuestaPerfilDocenteModel(
    val idPropuestaPerfil: String,
    val tituloPropuesta: String,
    val urlDocumento: String?,
    val estadoPropuesta: String,
    val observacionPropuesta: String?,
    val fechaRegistro: String
)