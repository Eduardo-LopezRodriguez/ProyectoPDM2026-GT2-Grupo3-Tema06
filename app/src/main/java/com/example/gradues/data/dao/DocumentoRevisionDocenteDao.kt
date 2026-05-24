// DocumentoRevisionDocenteDao.kt
package com.example.gradues.data.dao

import android.content.ContentValues
import android.database.Cursor
import android.util.Log
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.DocumentoRevisionDocenteModel

class DocumentoRevisionDocenteDao(private val dbHelper: DatabaseHelper) {

    companion object {
        private const val TAG = "DocumentoRevisionDocenteDao"
    }

    // Helper to get string from cursor
    private fun getString(cursor: Cursor, column: String): String {
        val index = cursor.getColumnIndexOrThrow(column)
        return if (cursor.isNull(index)) "" else cursor.getString(index)
    }

    // Helper to get int from cursor
    private fun getInt(cursor: Cursor, column: String): Int {
        val index = cursor.getColumnIndexOrThrow(column)
        return if (cursor.isNull(index)) 0 else cursor.getInt(index)
    }

    private fun getInternalUserId(userIdString: String): Int? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT idUsuario
            FROM usuario
            WHERE carnetUsuario = ? OR CAST(idUsuario AS TEXT) = ?
            LIMIT 1
            """.trimIndent(),
            arrayOf(userIdString, userIdString)
        )

        var idUsuario: Int? = null
        if (cursor.moveToFirst()) {
            idUsuario = cursor.getInt(cursor.getColumnIndexOrThrow("idUsuario"))
        }
        cursor.close()
        return idUsuario
    }

    fun obtenerDocumentosEnRevision(idDocente: String): List<DocumentoRevisionDocenteModel> {
        val documentos = mutableListOf<DocumentoRevisionDocenteModel>()
        val db = dbHelper.readableDatabase

        val internalIdDocente = getInternalUserId(idDocente)
        if (internalIdDocente == null) {
            Log.d(TAG, "Internal ID for docente $idDocente not found.")
            return emptyList()
        }

        Log.d(TAG, "Fetching documents for internal ID: $internalIdDocente")

        val cursor = db.rawQuery(
            """
            SELECT
                d.idDocumento,
                d.tituloDocumento AS nombreDocumento,
                d.tipoDocumento,
                d.versionDocumento,
                d.urlDocumento,
                COALESCE(d.estadoDocumento, 'Pendiente') AS estadoDocumento,
                COALESCE(d.observacionDocumento, '') AS observacionDocumento,
                d.fechaSubida AS fechaCarga,
                tg.nombreTrabajo,
                m.tipoModalidad AS nombreModalidad,
                u.carnetUsuario,
                COALESCE(u.primerNombreUsuario, '') || ' ' ||
                COALESCE(u.segundoNombreUsuario, '') || ' ' ||
                COALESCE(u.primerApellidoUsuario, '') || ' ' ||
                COALESCE(u.segundoApellidoUsuario, '') AS nombreEstudiante
            FROM documento d
            INNER JOIN trabajo_graduacion tg ON d.idTrabajoGraduacion = tg.idTrabajoGraduacion
            INNER JOIN modalidad m ON tg.idModalidad = m.idModalidad
            INNER JOIN alumno_trabajo at ON tg.idTrabajoGraduacion = at.idTrabajoGraduacion
            INNER JOIN usuario u ON at.idUsuario = u.idUsuario
            WHERE tg.idDocenteResponsable = ?
            AND d.estadoDocumento IN ('En revisión', 'Con observación') -- Filter for pending documents
            GROUP BY d.idDocumento, d.tituloDocumento, d.tipoDocumento, d.versionDocumento,
                     d.urlDocumento, d.estadoDocumento, d.observacionDocumento, d.fechaSubida,
                     tg.nombreTrabajo, m.tipoModalidad, u.carnetUsuario, nombreEstudiante
            ORDER BY d.fechaSubida DESC
            """.trimIndent(),
            arrayOf(internalIdDocente.toString())
        )

        while (cursor.moveToNext()) {
            documentos.add(
                DocumentoRevisionDocenteModel(
                    idDocumento = getString(cursor, "idDocumento"),
                    nombreDocumento = getString(cursor, "nombreDocumento"),
                    tipoDocumento = getString(cursor, "tipoDocumento"),
                    versionDocumento = getInt(cursor, "versionDocumento"),
                    urlDocumento = getString(cursor, "urlDocumento"),
                    estadoDocumento = getString(cursor, "estadoDocumento"),
                    observacionDocumento = getString(cursor, "observacionDocumento"),
                    fechaCarga = getString(cursor, "fechaCarga"),
                    nombreTrabajo = getString(cursor, "nombreTrabajo"),
                    nombreModalidad = getString(cursor, "nombreModalidad"),
                    carnetEstudiante = getString(cursor, "carnetUsuario"),
                    nombreEstudiante = getString(cursor, "nombreEstudiante")
                )
            )
        }
        cursor.close()
        Log.d(TAG, "Finished fetching documents. Total found: ${documentos.size}")
        return documentos
    }

    fun obtenerDetalleDocumento(idDocumento: String): DocumentoRevisionDocenteModel? {
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            """
            SELECT
                d.idDocumento,
                d.tituloDocumento AS nombreDocumento,
                d.tipoDocumento,
                d.versionDocumento,
                d.urlDocumento,
                COALESCE(d.estadoDocumento, 'Pendiente') AS estadoDocumento,
                COALESCE(d.observacionDocumento, '') AS observacionDocumento,
                d.fechaSubida AS fechaCarga,
                tg.nombreTrabajo,
                m.tipoModalidad AS nombreModalidad,
                u.carnetUsuario,
                COALESCE(u.primerNombreUsuario, '') || ' ' ||
                COALESCE(u.segundoNombreUsuario, '') || ' ' ||
                COALESCE(u.primerApellidoUsuario, '') || ' ' ||
                COALESCE(u.segundoApellidoUsuario, '') AS nombreEstudiante
            FROM documento d
            INNER JOIN trabajo_graduacion tg ON d.idTrabajoGraduacion = tg.idTrabajoGraduacion
            INNER JOIN modalidad m ON tg.idModalidad = m.idModalidad
            INNER JOIN alumno_trabajo at ON tg.idTrabajoGraduacion = at.idTrabajoGraduacion
            INNER JOIN usuario u ON at.idUsuario = u.idUsuario
            WHERE d.idDocumento = ?
            LIMIT 1
            """.trimIndent(),
            arrayOf(idDocumento)
        )

        var documento: DocumentoRevisionDocenteModel? = null
        if (cursor.moveToFirst()) {
            documento = DocumentoRevisionDocenteModel(
                idDocumento = getString(cursor, "idDocumento"),
                nombreDocumento = getString(cursor, "nombreDocumento"),
                tipoDocumento = getString(cursor, "tipoDocumento"),
                versionDocumento = getInt(cursor, "versionDocumento"),
                urlDocumento = getString(cursor, "urlDocumento"),
                estadoDocumento = getString(cursor, "estadoDocumento"),
                observacionDocumento = getString(cursor, "observacionDocumento"),
                fechaCarga = getString(cursor, "fechaCarga"),
                nombreTrabajo = getString(cursor, "nombreTrabajo"),
                nombreModalidad = getString(cursor, "nombreModalidad"),
                carnetEstudiante = getString(cursor, "carnetUsuario"),
                nombreEstudiante = getString(cursor, "nombreEstudiante")
            )
        }
        cursor.close()
        return documento
    }

    fun actualizarEstadoYObservacionDocumento(idDocumento: String, nuevoEstado: String, nuevaObservacion: String?): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("estadoDocumento", nuevoEstado)
            put("observacionDocumento", nuevaObservacion)
        }
        val rowsAffected = db.update("documento", values, "idDocumento = ?", arrayOf(idDocumento))
        return rowsAffected > 0
    }
}