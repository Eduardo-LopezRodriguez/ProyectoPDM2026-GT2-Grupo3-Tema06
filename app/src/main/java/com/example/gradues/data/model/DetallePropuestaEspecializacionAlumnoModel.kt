package com.example.gradues.data.model

data class DetallePropuestaEspecializacionAlumnoModel(
    val idPropuesta: Int,
    val idTrabajoGraduacion: Int,
    val tituloPropuesta: String,
    val descripcionPropuesta: String,
    val estadoPropuesta: String,
    val observacionPropuesta: String?,
    val urlArchivo: String?,
    val fechaRegistro: String
)
