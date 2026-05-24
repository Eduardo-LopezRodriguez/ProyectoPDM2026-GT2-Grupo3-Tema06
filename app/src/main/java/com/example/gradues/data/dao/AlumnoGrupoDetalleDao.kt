package com.example.gradues.data.dao

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.AlumnoDocumentoModel
import com.example.gradues.data.model.AlumnoGrupoDetalleModel
import com.example.gradues.data.model.AlumnoPropuestaModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    fun registrarPropuesta(valorSesion: String, titulo: String, descripcion: String): Boolean {
        val idTrabajoGraduacion = obtenerIdTrabajoInvestigacion(valorSesion) ?: return false
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("idTrabajoGraduacion", idTrabajoGraduacion)
            put("tituloPropuesta", titulo)
            put("descripcionPropuesta", descripcion)
            put("estadoPropuesta", "En revision")
            put("observacionPropuesta", "")
            putNull("urlArchivo")
            put("fechaRegistro", fechaActual())
        }

        val resultado = db.insert("propuesta_perfil", null, values)
        db.close()
        return resultado != -1L
    }

    fun obtenerPropuestasAlumno(valorSesion: String): List<AlumnoPropuestaModel> {
        val idTrabajoGraduacion = obtenerIdTrabajoInvestigacion(valorSesion) ?: return emptyList()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT
                idPropuesta,
                COALESCE(tituloPropuesta, ''),
                COALESCE(descripcionPropuesta, ''),
                COALESCE(estadoPropuesta, 'En revision'),
                COALESCE(observacionPropuesta, ''),
                urlArchivo,
                COALESCE(fechaRegistro, '')
            FROM propuesta_perfil
            WHERE idTrabajoGraduacion = ?
            ORDER BY fechaRegistro DESC, idPropuesta DESC
            """.trimIndent(),
            arrayOf(idTrabajoGraduacion.toString())
        )

        val propuestas = mutableListOf<AlumnoPropuestaModel>()
        while (cursor.moveToNext()) {
            propuestas.add(
                AlumnoPropuestaModel(
                    idPropuesta = cursor.getInt(0),
                    tituloPropuesta = cursor.getString(1) ?: "",
                    descripcionPropuesta = cursor.getString(2) ?: "",
                    estadoPropuesta = cursor.getString(3) ?: "En revision",
                    observacionPropuesta = cursor.getString(4) ?: "",
                    urlArchivo = cursor.getString(5),
                    fechaRegistro = cursor.getString(6) ?: ""
                )
            )
        }

        cursor.close()
        db.close()
        return propuestas
    }

    fun obtenerUltimoDocumentoTesis(valorSesion: String): AlumnoDocumentoModel? {
        val idTrabajoGraduacion = obtenerIdTrabajoInvestigacion(valorSesion) ?: return null
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT
                idDocumento,
                idTrabajoGraduacion,
                COALESCE(tipoDocumento, 'Tesis'),
                COALESCE(tituloDocumento, ''),
                urlDocumento,
                COALESCE(estadoDocumento, 'En revision'),
                COALESCE(observacionDocumento, ''),
                COALESCE(versionDocumento, 1),
                COALESCE(fechaSubida, '')
            FROM documento
            WHERE idTrabajoGraduacion = ?
            ORDER BY versionDocumento DESC, idDocumento DESC
            LIMIT 1
            """.trimIndent(),
            arrayOf(idTrabajoGraduacion.toString())
        )

        val documento = if (cursor.moveToFirst()) {
            AlumnoDocumentoModel(
                idDocumento = cursor.getInt(0),
                idTrabajoGraduacion = cursor.getInt(1),
                tipoDocumento = cursor.getString(2) ?: "Tesis",
                tituloDocumento = cursor.getString(3) ?: "",
                urlDocumento = cursor.getString(4),
                estadoDocumento = cursor.getString(5) ?: "En revision",
                observacionDocumento = cursor.getString(6) ?: "",
                versionDocumento = cursor.getInt(7),
                fechaSubida = cursor.getString(8) ?: ""
            )
        } else {
            null
        }

        cursor.close()
        db.close()
        return documento
    }

    fun registrarNuevaVersionTesis(valorSesion: String, titulo: String): Boolean {
        val idTrabajoGraduacion = obtenerIdTrabajoInvestigacion(valorSesion) ?: return false
        val siguienteVersion = obtenerSiguienteVersionDocumento(idTrabajoGraduacion)
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("idTrabajoGraduacion", idTrabajoGraduacion)
            put("tipoDocumento", "Tesis")
            put("tituloDocumento", titulo)
            putNull("urlDocumento")
            put("estadoDocumento", "En revision")
            put("observacionDocumento", "")
            put("versionDocumento", siguienteVersion)
            put("fechaSubida", fechaActual())
        }

        val resultado = db.insert("documento", null, values)
        db.close()
        return resultado != -1L
    }

    private fun obtenerIdTrabajoInvestigacion(valorSesion: String): Int? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT at.idTrabajoGraduacion
            FROM alumno_trabajo at
            INNER JOIN usuario u
                ON u.idUsuario = at.idUsuario
            INNER JOIN trabajo_graduacion t
                ON t.idTrabajoGraduacion = at.idTrabajoGraduacion
            WHERE t.idModalidad = 1
              AND (
                  CAST(at.idUsuario AS TEXT) = ?
                  OR UPPER(TRIM(u.carnetUsuario)) = UPPER(TRIM(?))
              )
            LIMIT 1
            """.trimIndent(),
            arrayOf(valorSesion, valorSesion)
        )

        val idTrabajo = if (cursor.moveToFirst()) cursor.getInt(0) else null
        cursor.close()
        db.close()
        return idTrabajo
    }

    private fun obtenerSiguienteVersionDocumento(idTrabajoGraduacion: Int): Int {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT COALESCE(MAX(versionDocumento), 0) + 1
            FROM documento
            WHERE idTrabajoGraduacion = ?
            """.trimIndent(),
            arrayOf(idTrabajoGraduacion.toString())
        )

        val version = if (cursor.moveToFirst()) cursor.getInt(0) else 1
        cursor.close()
        db.close()
        return version
    }

    private fun fechaActual(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
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
