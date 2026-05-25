package com.example.gradues.data.dao

import android.content.ContentValues
import android.database.Cursor
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.HistorialTesinaAlumnoModel
import com.example.gradues.data.model.TesinaEspecializacionAlumnoModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TesinaEspecializacionAlumnoDao(
    private val dbHelper: DatabaseHelper
) {

    fun obtenerTesinaActiva(idSesion: String): TesinaEspecializacionAlumnoModel? {
        val trabajo = obtenerTrabajoEspecializacionActivo(idSesion) ?: return null
        return TesinaEspecializacionAlumnoModel(
            idTrabajoGraduacion = trabajo.idTrabajoGraduacion,
            tituloTrabajo = trabajo.tituloTrabajo,
            documentoActual = obtenerUltimaTesinaPorTrabajo(trabajo.idTrabajoGraduacion)
        )
    }

    fun subirNuevaVersion(idSesion: String, urlDocumento: String): Boolean {
        val trabajo = obtenerTrabajoEspecializacionActivo(idSesion) ?: return false
        val db = dbHelper.writableDatabase
        val siguienteVersion = obtenerSiguienteVersion(trabajo.idTrabajoGraduacion)
        val fecha = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())

        val values = ContentValues().apply {
            put("idTrabajoGraduacion", trabajo.idTrabajoGraduacion)
            put("tipoDocumento", "Tesina")
            put("tituloDocumento", trabajo.tituloTrabajo)
            put("urlDocumento", urlDocumento)
            put("estadoDocumento", "Pendiente")
            putNull("observacionDocumento")
            put("versionDocumento", siguienteVersion)
            put("fechaSubida", fecha)
        }

        return db.insert("documento", null, values) != -1L
    }

    fun obtenerHistorialTesinaActiva(idSesion: String): List<HistorialTesinaAlumnoModel> {
        val trabajo = obtenerTrabajoEspecializacionActivo(idSesion) ?: return emptyList()
        return obtenerHistorialPorTrabajo(trabajo.idTrabajoGraduacion)
    }

    private fun obtenerTrabajoEspecializacionActivo(idSesion: String): TrabajoTesinaActivo? {
        val db = dbHelper.readableDatabase
        val datoSesion = idSesion.trim()
        val cursor = db.rawQuery(
            """
            SELECT
                tg.idTrabajoGraduacion,
                COALESCE(NULLIF(TRIM(sgtge.temaAsignado), ''), tg.nombreTrabajo, 'Tesina del grupo') AS tituloTrabajo
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
              AND UPPER(COALESCE(tg.estadoTrabajo, 'ACTIVO')) = 'ACTIVO'
              AND UPPER(COALESCE(at.estadoAlumnoTrabajo, 'ACTIVO')) = 'ACTIVO'
            ORDER BY tg.idTrabajoGraduacion DESC
            LIMIT 1
            """.trimIndent(),
            arrayOf(datoSesion, datoSesion, datoSesion, datoSesion)
        )

        val trabajo = if (cursor.moveToFirst()) {
            TrabajoTesinaActivo(
                idTrabajoGraduacion = cursor.getInt(cursor.getColumnIndexOrThrow("idTrabajoGraduacion")),
                tituloTrabajo = getString(cursor, "tituloTrabajo")
            )
        } else {
            null
        }

        cursor.close()
        return trabajo
    }

    private fun obtenerUltimaTesinaPorTrabajo(idTrabajoGraduacion: Int): HistorialTesinaAlumnoModel? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT *
            FROM documento
            WHERE idTrabajoGraduacion = ?
              AND (
                    LOWER(COALESCE(tipoDocumento, '')) LIKE '%tesina%'
                    OR LOWER(COALESCE(tipoDocumento, '')) LIKE '%tesis%'
                    OR LOWER(COALESCE(tituloDocumento, '')) LIKE '%tesina%'
                    OR LOWER(COALESCE(tituloDocumento, '')) LIKE '%tesis%'
                  )
            ORDER BY COALESCE(versionDocumento, 0) DESC, idDocumento DESC
            LIMIT 1
            """.trimIndent(),
            arrayOf(idTrabajoGraduacion.toString())
        )

        val documento = if (cursor.moveToFirst()) mapDocumento(cursor) else null
        cursor.close()
        return documento
    }

    private fun obtenerHistorialPorTrabajo(idTrabajoGraduacion: Int): List<HistorialTesinaAlumnoModel> {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT *
            FROM documento
            WHERE idTrabajoGraduacion = ?
              AND (
                    LOWER(COALESCE(tipoDocumento, '')) LIKE '%tesina%'
                    OR LOWER(COALESCE(tipoDocumento, '')) LIKE '%tesis%'
                    OR LOWER(COALESCE(tituloDocumento, '')) LIKE '%tesina%'
                    OR LOWER(COALESCE(tituloDocumento, '')) LIKE '%tesis%'
                  )
            ORDER BY COALESCE(versionDocumento, 0) DESC, idDocumento DESC
            """.trimIndent(),
            arrayOf(idTrabajoGraduacion.toString())
        )

        val historial = mutableListOf<HistorialTesinaAlumnoModel>()
        while (cursor.moveToNext()) {
            historial.add(mapDocumento(cursor))
        }
        cursor.close()
        return historial
    }

    private fun obtenerSiguienteVersion(idTrabajoGraduacion: Int): Int {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT COALESCE(MAX(versionDocumento), 0) + 1 AS siguienteVersion
            FROM documento
            WHERE idTrabajoGraduacion = ?
              AND (
                    LOWER(COALESCE(tipoDocumento, '')) LIKE '%tesina%'
                    OR LOWER(COALESCE(tipoDocumento, '')) LIKE '%tesis%'
                    OR LOWER(COALESCE(tituloDocumento, '')) LIKE '%tesina%'
                    OR LOWER(COALESCE(tituloDocumento, '')) LIKE '%tesis%'
                  )
            """.trimIndent(),
            arrayOf(idTrabajoGraduacion.toString())
        )

        val version = if (cursor.moveToFirst()) {
            cursor.getInt(cursor.getColumnIndexOrThrow("siguienteVersion"))
        } else {
            1
        }
        cursor.close()
        return version
    }

    private fun mapDocumento(cursor: Cursor): HistorialTesinaAlumnoModel {
        return HistorialTesinaAlumnoModel(
            idDocumento = cursor.getInt(cursor.getColumnIndexOrThrow("idDocumento")),
            idTrabajoGraduacion = cursor.getInt(cursor.getColumnIndexOrThrow("idTrabajoGraduacion")),
            tipoDocumento = getString(cursor, "tipoDocumento").ifBlank { "Tesina" },
            tituloDocumento = getString(cursor, "tituloDocumento").ifBlank { "Tesina del grupo" },
            urlDocumento = getNullableString(cursor, "urlDocumento"),
            estadoDocumento = getString(cursor, "estadoDocumento").ifBlank { "Pendiente" },
            observacionDocumento = getNullableString(cursor, "observacionDocumento"),
            versionDocumento = getNullableInt(cursor, "versionDocumento"),
            fechaSubida = getNullableString(cursor, "fechaSubida")
        )
    }

    private fun getString(cursor: Cursor, column: String): String {
        val index = cursor.getColumnIndexOrThrow(column)
        return if (cursor.isNull(index)) "" else cursor.getString(index)
    }

    private fun getNullableString(cursor: Cursor, column: String): String? {
        val index = cursor.getColumnIndexOrThrow(column)
        return if (cursor.isNull(index)) null else cursor.getString(index)
    }

    private fun getNullableInt(cursor: Cursor, column: String): Int? {
        val index = cursor.getColumnIndexOrThrow(column)
        return if (cursor.isNull(index)) null else cursor.getInt(index)
    }

    private data class TrabajoTesinaActivo(
        val idTrabajoGraduacion: Int,
        val tituloTrabajo: String
    )
}
