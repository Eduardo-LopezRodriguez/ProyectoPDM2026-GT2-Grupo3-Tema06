package com.example.gradues.data.dao

import android.database.Cursor
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.DashboardAdminModel

class DashboardAdminDao(
    private val dbHelper: DatabaseHelper
) {

    fun obtenerDashboardAdmin(idUsuarioSesion: String): DashboardAdminModel? {
        val db = dbHelper.readableDatabase
        val datoUsuario = idUsuarioSesion.trim()

        val cursor = db.rawQuery(
            """
            SELECT
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
              AND r.nombreRol = 'Administrador'
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

        var model: DashboardAdminModel? = null

        if (cursor.moveToFirst()) {
            model = DashboardAdminModel(
                idUsuario = getString(cursor, "IdUsuario"),
                nombreCompleto = getString(cursor, "NombreCompleto").ifBlank { "Administrador" },
                correo = getString(cursor, "Correo"),
                totalUsuarios = contarUsuariosActivos(),
                totalAlumnos = contarUsuariosPorRol("Alumno"),
                totalDocentes = contarUsuariosPorRol("Docente"),
                totalTrabajosActivos = contarTrabajosActivos(),
                solicitudesPendientes = contarSolicitudesPorEstado("Pendiente"),
                solicitudesAprobadas = contarSolicitudesPorEstado("Aprobada")
            )
        }

        cursor.close()
        return model
    }

    private fun contarUsuariosActivos(): Int {
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            """
            SELECT COUNT(*) AS Total
            FROM usuario
            WHERE COALESCE(estadoUsuario, 'Activo') = 'Activo'
            """.trimIndent(),
            null
        )

        val total = obtenerTotal(cursor)
        cursor.close()
        return total
    }

    private fun contarUsuariosPorRol(nombreRol: String): Int {
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            """
            SELECT COUNT(*) AS Total
            FROM usuario u
            INNER JOIN rol r
                ON r.idRol = u.idRol
            WHERE r.nombreRol = ?
              AND COALESCE(u.estadoUsuario, 'Activo') = 'Activo'
            """.trimIndent(),
            arrayOf(nombreRol)
        )

        val total = obtenerTotal(cursor)
        cursor.close()
        return total
    }

    private fun contarTrabajosActivos(): Int {
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            """
            SELECT COUNT(*) AS Total
            FROM trabajo_graduacion
            WHERE COALESCE(estadoTrabajo, 'Activo') = 'Activo'
            """.trimIndent(),
            null
        )

        val total = obtenerTotal(cursor)
        cursor.close()
        return total
    }

    private fun contarSolicitudesPorEstado(estado: String): Int {
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            """
            SELECT COUNT(*) AS Total
            FROM solicitud_modalidad
            WHERE estadoSolicitud = ?
            """.trimIndent(),
            arrayOf(estado)
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