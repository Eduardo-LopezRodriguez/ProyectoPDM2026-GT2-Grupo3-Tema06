package com.example.gradues.data.model

data class AlumnoBitacoraModel(
    val idBitacora: Int,
    val idProyectoPasantia: Int,
    val fechaActividad: String,
    val tituloActividad: String,
    val descripcionActividad: String,
    val totalHorasTrabajadas: Int,
    val estadoBitacora: String,
    val observacionBitacora: String
)
