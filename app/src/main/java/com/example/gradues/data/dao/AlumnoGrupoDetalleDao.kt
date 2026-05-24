package com.example.gradues.data.dao

import android.database.sqlite.SQLiteDatabase
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.AlumnoGrupoDetalleModel

class AlumnoGrupoDetalleDao(private val dbHelper: DatabaseHelper) {

    fun obtenerGrupoInvestigacionAlumno(valorSesion: String): AlumnoGrupoDetalleModel? {
        val db = dbHelper.readableDatabase

        val sql = """
        SELECT
            at.idAlumnoTrabajo,
            at.idTrabajoGraduacion,
            COALESCE(g.codigoGrupoTGI, 'Sin código') AS codigoGrupoTGI,
            COALESCE(t.nombreTrabajo, 'Sin tema asignado') AS nombreTrabajo,
            COALESCE(t.estadoTrabajo, 'Sin estado') AS estadoTrabajo,
            COALESCE(d.nombreUsuario, 'Docente pendiente') AS nombreDocente
        FROM alumno_trabajo at
        INNER JOIN usuario u
            ON u.idUsuario = at.idUsuario
        INNER JOIN trabajo_graduacion t
            ON t.idTrabajoGraduacion = at.idTrabajoGraduacion
        LEFT JOIN grupo_tgi g
            ON g.idTrabajoGraduacion = t.idTrabajoGraduacion
        LEFT JOIN usuario d
            ON d.idUsuario = t.idDocenteResponsable
        WHERE t.idModalidad = 1
          AND (
              CAST(at.idUsuario AS TEXT) = ?
              OR UPPER(TRIM(u.carnetUsuario)) = UPPER(TRIM(?))
          )
        LIMIT 1
    """.trimIndent()

        val cursor = db.rawQuery(sql, arrayOf(valorSesion, valorSesion))

        if (!cursor.moveToFirst()) {
            cursor.close()
            return null
        }

        val idAlumnoTrabajo = cursor.getInt(cursor.getColumnIndexOrThrow("idAlumnoTrabajo"))
        val idTrabajoGraduacion = cursor.getInt(cursor.getColumnIndexOrThrow("idTrabajoGraduacion"))
        val codigoGrupo = cursor.getString(cursor.getColumnIndexOrThrow("codigoGrupoTGI"))
        val temaTrabajo = cursor.getString(cursor.getColumnIndexOrThrow("nombreTrabajo"))
        val estadoTrabajo = cursor.getString(cursor.getColumnIndexOrThrow("estadoTrabajo"))
        val nombreDocente = cursor.getString(cursor.getColumnIndexOrThrow("nombreDocente"))
        cursor.close()

        val integrantesTexto = obtenerIntegrantesTexto(db, idTrabajoGraduacion)
        val cantidadIntegrantes = if (integrantesTexto.isBlank()) 0 else integrantesTexto.split("\n").size

        return AlumnoGrupoDetalleModel(
            nombreGrupo = "Grupo de Investigación",
            codigoGrupo = codigoGrupo,
            temaTrabajo = temaTrabajo,
            nombreDocente = nombreDocente,
            integrantesTexto = integrantesTexto.ifBlank { "Integrantes no disponibles" },
            cantidadIntegrantes = cantidadIntegrantes,
            estadoGeneral = estadoTrabajo,
            notaEtapa1 = obtenerNotaEtapa(db, idAlumnoTrabajo, 1),
            notaEtapa2 = obtenerNotaEtapa(db, idAlumnoTrabajo, 2),
            notaEtapa3 = obtenerNotaEtapa(db, idAlumnoTrabajo, 3),
            notaEtapa4 = obtenerNotaEtapa(db, idAlumnoTrabajo, 4),
            resumenPropuestas = obtenerResumenPropuestas(db, idTrabajoGraduacion),
            descripcionTesis = obtenerDescripcionTesis(db, idTrabajoGraduacion, temaTrabajo),
            estadoTesis = obtenerEstadoTesis(db, idTrabajoGraduacion),
            ultimaVersionTesis = obtenerUltimaVersionTesis(db, idTrabajoGraduacion)
        )
    }

    private fun obtenerIntegrantesTexto(db: SQLiteDatabase, idTrabajoGraduacion: Int): String {
        val sql = """
            SELECT u.nombreUsuario, u.carnetUsuario
            FROM alumno_trabajo at
            INNER JOIN usuario u
                ON u.idUsuario = at.idUsuario
            WHERE at.idTrabajoGraduacion = ?
            ORDER BY u.nombreUsuario
        """.trimIndent()

        val cursor = db.rawQuery(sql, arrayOf(idTrabajoGraduacion.toString()))
        val integrantes = mutableListOf<String>()

        while (cursor.moveToNext()) {
            val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombreUsuario"))
            val carnet = cursor.getString(cursor.getColumnIndexOrThrow("carnetUsuario"))
            integrantes.add("$nombre ($carnet)")
        }

        cursor.close()
        return integrantes.joinToString("\n")
    }

    private fun obtenerNotaEtapa(db: SQLiteDatabase, idAlumnoTrabajo: Int, numeroEtapa: Int): String {
        val sql = """
            SELECT nota
            FROM nota_etapa
            WHERE idAlumnoTrabajo = ?
              AND numeroEtapa = ?
            LIMIT 1
        """.trimIndent()

        val cursor = db.rawQuery(sql, arrayOf(idAlumnoTrabajo.toString(), numeroEtapa.toString()))
        val nota = if (cursor.moveToFirst()) {
            if (cursor.isNull(0)) "--" else cursor.getDouble(0).toString()
        } else {
            "--"
        }
        cursor.close()
        return nota
    }

    private fun obtenerResumenPropuestas(db: SQLiteDatabase, idTrabajoGraduacion: Int): String {
        val sql = """
        SELECT
            COUNT(*) AS totalPropuestas,
            SUM(CASE WHEN estadoPropuesta = 'Seleccionada' THEN 1 ELSE 0 END) AS seleccionadas
        FROM propuesta_perfil
        WHERE idTrabajoGraduacion = ?
    """.trimIndent()

        val cursor = db.rawQuery(sql, arrayOf(idTrabajoGraduacion.toString()))

        val resumen = if (cursor.moveToFirst()) {
            val total = cursor.getInt(cursor.getColumnIndexOrThrow("totalPropuestas"))
            val seleccionadas = cursor.getInt(cursor.getColumnIndexOrThrow("seleccionadas"))
            "$total propuesta(s) registradas. $seleccionadas seleccionada(s)"
        } else {
            "Sin propuestas registradas"
        }

        cursor.close()
        return resumen
    }

    private fun obtenerDescripcionTesis(
        db: SQLiteDatabase,
        idTrabajoGraduacion: Int,
        temaTrabajo: String
    ): String {
        val sql = """
        SELECT tituloDocumento
        FROM documento
        WHERE idTrabajoGraduacion = ?
        ORDER BY versionDocumento DESC, idDocumento DESC
        LIMIT 1
    """.trimIndent()

        val cursor = db.rawQuery(sql, arrayOf(idTrabajoGraduacion.toString()))

        val descripcion = if (cursor.moveToFirst()) {
            cursor.getString(cursor.getColumnIndexOrThrow("tituloDocumento")).orEmpty().ifBlank {
                temaTrabajo
            }
        } else {
            temaTrabajo
        }

        cursor.close()
        return descripcion
    }

    private fun obtenerEstadoTesis(db: SQLiteDatabase, idTrabajoGraduacion: Int): String {
        val sql = """
        SELECT estadoDocumento
        FROM documento
        WHERE idTrabajoGraduacion = ?
        ORDER BY versionDocumento DESC, idDocumento DESC
        LIMIT 1
    """.trimIndent()

        val cursor = db.rawQuery(sql, arrayOf(idTrabajoGraduacion.toString()))

        val estado = if (cursor.moveToFirst()) {
            cursor.getString(cursor.getColumnIndexOrThrow("estadoDocumento")).orEmpty()
                .ifBlank { "No subida" }
        } else {
            "No subida"
        }

        cursor.close()
        return estado
    }

    private fun obtenerUltimaVersionTesis(db: SQLiteDatabase, idTrabajoGraduacion: Int): String {
        val sql = """
        SELECT versionDocumento, fechaSubida
        FROM documento
        WHERE idTrabajoGraduacion = ?
        ORDER BY versionDocumento DESC, idDocumento DESC
        LIMIT 1
    """.trimIndent()

        val cursor = db.rawQuery(sql, arrayOf(idTrabajoGraduacion.toString()))

        val versionTexto = if (cursor.moveToFirst()) {
            val version = if (cursor.isNull(cursor.getColumnIndexOrThrow("versionDocumento"))) {
                null
            } else {
                cursor.getInt(cursor.getColumnIndexOrThrow("versionDocumento"))
            }

            val fecha = cursor.getString(cursor.getColumnIndexOrThrow("fechaSubida")).orEmpty()

            when {
                version != null && fecha.isNotBlank() -> "Versión $version - $fecha"
                version != null -> "Versión $version"
                else -> "No enviada"
            }
        } else {
            "No enviada"
        }

        cursor.close()
        return versionTexto
    }
}