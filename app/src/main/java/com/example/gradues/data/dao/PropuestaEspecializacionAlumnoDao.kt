package com.example.gradues.data.dao

import android.content.ContentValues
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.DetallePropuestaEspecializacionAlumnoModel
import com.example.gradues.data.model.PropuestaEspecializacionAlumnoModel

class PropuestaEspecializacionAlumnoDao(
    private val dbHelper: DatabaseHelper
) {

    fun obtenerIdTrabajoEspecializacionActivo(idSesion: String): Int? {
        val db = dbHelper.readableDatabase
        val datoSesion = idSesion.trim()

        val cursor = db.rawQuery(
            """
                SELECT tg.idTrabajoGraduacion
                FROM usuario u
                INNER JOIN alumno_trabajo at
                    ON at.idUsuario = u.idUsuario
                INNER JOIN trabajo_graduacion tg
                    ON tg.idTrabajoGraduacion = at.idTrabajoGraduacion
                INNER JOIN modalidad m
                    ON m.idModalidad = tg.idModalidad
                INNER JOIN subgrupo_tge sgtge
                    ON sgtge.idTrabajoGraduacion = tg.idTrabajoGraduacion
                WHERE (
                        CAST(u.idUsuario AS TEXT) = ?
                        OR UPPER(COALESCE(u.carnetUsuario, '')) = UPPER(?)
                        OR UPPER(COALESCE(u.correoUsuario, '')) = UPPER(?)
                        OR UPPER(COALESCE(u.nombreUsuario, '')) = UPPER(?)
                      )
                  AND (
                        tg.idModalidad = 2
                        OR LOWER(COALESCE(m.tipoModalidad, '')) LIKE '%especializ%'
                      )
                  AND COALESCE(tg.estadoTrabajo, 'Activo') = 'Activo'
                ORDER BY tg.idTrabajoGraduacion DESC
                LIMIT 1
            """.trimIndent(),
            arrayOf(datoSesion, datoSesion, datoSesion, datoSesion)
        )

        val idTrabajo = if (cursor.moveToFirst()) {
            cursor.getInt(cursor.getColumnIndexOrThrow("idTrabajoGraduacion"))
        } else {
            null
        }

        cursor.close()
        return idTrabajo
    }

    fun insertarPropuesta(propuesta: PropuestaEspecializacionAlumnoModel): Boolean {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put("idTrabajoGraduacion", propuesta.idTrabajoGraduacion)
            put("tituloPropuesta", propuesta.tituloPropuesta)
            put("descripcionPropuesta", propuesta.descripcionPropuesta)
            put("estadoPropuesta", propuesta.estadoPropuesta)
            put("observacionPropuesta", propuesta.observacionPropuesta)
            put("urlArchivo", propuesta.urlArchivo)
            put("fechaRegistro", propuesta.fechaRegistro)
        }

        return db.insert("propuesta_perfil", null, values) != -1L
    }

    fun obtenerPropuestasEspecializacionActiva(idSesion: String): List<DetallePropuestaEspecializacionAlumnoModel> {
        val idTrabajo = obtenerIdTrabajoEspecializacionActivo(idSesion) ?: return emptyList()
        return obtenerPropuestasPorTrabajo(idTrabajo)
    }

    private fun obtenerPropuestasPorTrabajo(idTrabajoGraduacion: Int): List<DetallePropuestaEspecializacionAlumnoModel> {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            """
                SELECT
                    idPropuesta,
                    idTrabajoGraduacion,
                    COALESCE(tituloPropuesta, 'Sin título') AS tituloPropuesta,
                    COALESCE(descripcionPropuesta, 'Sin descripción') AS descripcionPropuesta,
                    COALESCE(estadoPropuesta, 'Sin estado') AS estadoPropuesta,
                    observacionPropuesta,
                    urlArchivo,
                    COALESCE(fechaRegistro, '') AS fechaRegistro
                FROM propuesta_perfil
                WHERE idTrabajoGraduacion = ?
                ORDER BY fechaRegistro DESC, idPropuesta DESC
            """.trimIndent(),
            arrayOf(idTrabajoGraduacion.toString())
        )

        val propuestas = mutableListOf<DetallePropuestaEspecializacionAlumnoModel>()
        while (cursor.moveToNext()) {
            propuestas.add(
                DetallePropuestaEspecializacionAlumnoModel(
                    idPropuesta = cursor.getInt(cursor.getColumnIndexOrThrow("idPropuesta")),
                    idTrabajoGraduacion = cursor.getInt(cursor.getColumnIndexOrThrow("idTrabajoGraduacion")),
                    tituloPropuesta = cursor.getString(cursor.getColumnIndexOrThrow("tituloPropuesta")),
                    descripcionPropuesta = cursor.getString(cursor.getColumnIndexOrThrow("descripcionPropuesta")),
                    estadoPropuesta = cursor.getString(cursor.getColumnIndexOrThrow("estadoPropuesta")),
                    observacionPropuesta = getNullableString(cursor, "observacionPropuesta"),
                    urlArchivo = getNullableString(cursor, "urlArchivo"),
                    fechaRegistro = cursor.getString(cursor.getColumnIndexOrThrow("fechaRegistro"))
                )
            )
        }

        cursor.close()
        return propuestas
    }

    private fun getNullableString(cursor: android.database.Cursor, column: String): String? {
        val index = cursor.getColumnIndexOrThrow(column)
        return if (cursor.isNull(index)) null else cursor.getString(index)
    }
}
