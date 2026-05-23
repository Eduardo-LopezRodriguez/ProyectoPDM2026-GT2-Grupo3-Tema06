package com.example.gradues.data.dao

import android.database.Cursor
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.DashboardDocenteModel

class DashboardDocenteDao(
    private val dbHelper: DatabaseHelper
) {

    fun obtenerDashboardDocente(idUsuarioSesion: String): DashboardDocenteModel? {
        val db = dbHelper.readableDatabase
        val datoUsuario = idUsuarioSesion.trim()

        val cursor = db.rawQuery(
            """
                SELECT
                    u.idUsuario AS IdUsuarioInterno,
                    COALESCE(u.carnetUsuario, CAST(u.idUsuario AS TEXT)) AS IdUsuario,
                    TRIM(
                        COALESCE(u.primerNombreUsuario, '') || ' ' ||
                        COALESCE(u.segundoNombreUsuario || ' ', '') ||
                        COALESCE(u.primerApellidoUsuario, '') || ' ' ||
                        COALESCE(u.segundoApellidoUsuario, '')
                    ) AS NombreCompleto,
                    COALESCE(u.correoUsuario, '') AS Correo
                FROM usuario u
                INNER JOIN rol r
                    ON r.idRol = u.idRol
                WHERE (
                        CAST(u.idUsuario AS TEXT) = ?
                        OR UPPER(u.carnetUsuario) = UPPER(?)
                        OR UPPER(u.correoUsuario) = UPPER(?)
                        OR UPPER(u.nombreUsuario) = UPPER(?)
                      )
                  AND r.nombreRol IN ('Docente', 'Administrador')
                  AND COALESCE(u.estadoUsuario, 'Activo') = 'Activo'
                LIMIT 1
                """.trimIndent(),
            arrayOf(
                datoUsuario,
                datoUsuario,
                datoUsuario,
                datoUsuario
            )
        )

        var model: DashboardDocenteModel? = null

        if (cursor.moveToFirst()) {
            val idUsuarioInterno = cursor.getInt(cursor.getColumnIndexOrThrow("IdUsuarioInterno"))

            model = DashboardDocenteModel(
                idUsuario = getString(cursor, "IdUsuario"),
                nombreCompleto = getString(cursor, "NombreCompleto").ifBlank { "Docente" },
                correo = getString(cursor, "Correo"),
                totalInvestigacion = contarTrabajosPorModalidad(idUsuarioInterno, 1),
                totalEspecializacion = contarTrabajosPorModalidad(idUsuarioInterno, 2),
                totalPasantia = contarTrabajosPorModalidad(idUsuarioInterno, 3),
                solicitudesPendientes = contarSolicitudesPendientes(),
                documentosRevision = contarDocumentosEnRevision(idUsuarioInterno),
                bitacorasPendientes = contarBitacorasPendientes(idUsuarioInterno)
            )
        }

        cursor.close()
        return model
    }

    private fun contarTrabajosPorModalidad(idDocente: Int, idModalidad: Int): Int {
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            """
                SELECT COUNT(*) AS Total
                FROM trabajo_graduacion
                WHERE idDocenteResponsable = ?
                  AND idModalidad = ?
                  AND COALESCE(estadoTrabajo, 'Activo') = 'Activo'
                """.trimIndent(),
            arrayOf(idDocente.toString(), idModalidad.toString())
        )

        val total = obtenerTotal(cursor)
        cursor.close()
        return total
    }

    private fun contarSolicitudesPendientes(): Int {
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            """
                SELECT COUNT(*) AS Total
                FROM solicitud_modalidad
                WHERE estadoSolicitud = 'Pendiente'
                """.trimIndent(),
            null
        )

        val total = obtenerTotal(cursor)
        cursor.close()
        return total
    }

    private fun contarDocumentosEnRevision(idDocente: Int): Int {
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            """
                SELECT COUNT(*) AS Total
                FROM documento d
                INNER JOIN trabajo_graduacion tg
                    ON tg.idTrabajoGraduacion = d.idTrabajoGraduacion
                WHERE tg.idDocenteResponsable = ?
                  AND d.estadoDocumento IN ('En revisión', 'Con observación')
                """.trimIndent(),
            arrayOf(idDocente.toString())
        )

        val total = obtenerTotal(cursor)
        cursor.close()
        return total
    }

    private fun contarBitacorasPendientes(idDocente: Int): Int {
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            """
                SELECT COUNT(*) AS Total
                FROM bitacora b
                INNER JOIN proyecto_pasantia pp
                    ON pp.idProyectoPasantia = b.idProyectoPasantia
                INNER JOIN trabajo_graduacion tg
                    ON tg.idTrabajoGraduacion = pp.idTrabajoGraduacion
                WHERE tg.idDocenteResponsable = ?
                  AND b.estadoBitacora = 'Pendiente'
                """.trimIndent(),
            arrayOf(idDocente.toString())
        )

        val total = obtenerTotal(cursor)
        cursor.close()
        return total
    }

    private fun obtenerTotal(cursor: Cursor): Int {
        var total = 0

        if (cursor.moveToFirst()) {
            total = cursor.getInt(cursor.getColumnIndexOrThrow("Total"))
        }

        return total
    }

    private fun getString(cursor: Cursor, column: String): String {
        val index = cursor.getColumnIndexOrThrow(column)
        return if (cursor.isNull(index)) "" else cursor.getString(index)
    }
}