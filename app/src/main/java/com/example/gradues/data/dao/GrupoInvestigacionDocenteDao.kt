// GrupoInvestigacionDocenteDao.kt
package com.example.gradues.data.dao

import android.database.Cursor
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.DocumentoInvestigacionDocenteModel
import com.example.gradues.data.model.EstudianteSubgrupoModel
import com.example.gradues.data.model.GrupoInvestigacionDocenteModel
import com.example.gradues.data.model.NotaEtapaDocenteModel
import com.example.gradues.data.model.PropuestaPerfilDocenteModel
import android.util.Log
import com.example.gradues.data.dao.CursoEspecializacionDocenteDao.Companion.STAGE_ID_TO_NAME

class GrupoInvestigacionDocenteDao(private val dbHelper: DatabaseHelper) {

    companion object {
        private const val TAG = "GrupoInvestigacionDocenteDao"
    }

    // Helper to get string from cursor
    private fun getString(cursor: Cursor, column: String): String {
        val index = cursor.getColumnIndexOrThrow(column)
        return if (cursor.isNull(index)) "" else cursor.getString(index)
    }

    // Helper to get double from cursor
    private fun getDouble(cursor: Cursor, column: String): Double? {
        val index = cursor.getColumnIndexOrThrow(column)
        return if (cursor.isNull(index)) null else cursor.getDouble(index)
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

    fun obtenerGruposInvestigacionAsignados(idDocente: String): List<GrupoInvestigacionDocenteModel> {
        val grupos = mutableListOf<GrupoInvestigacionDocenteModel>()
        val db = dbHelper.readableDatabase

        Log.d(TAG, "Attempting to fetch groups for idDocente: $idDocente")

        val internalIdDocente = getInternalUserId(idDocente)
        if (internalIdDocente == null) {
            Log.d(TAG, "Internal ID for docente $idDocente not found.")
            return emptyList()
        }

        val cursor = db.rawQuery(
            """
            SELECT
                g.idGrupoTGI,
                tg.idTrabajoGraduacion,
                g.codigoGrupoTGI,
                tg.nombreTrabajo,
                COALESCE(tg.estadoTrabajo, 'Activo') AS estadoGrupo
            FROM grupo_tgi g
            INNER JOIN trabajo_graduacion tg ON g.idTrabajoGraduacion = tg.idTrabajoGraduacion
            WHERE tg.idDocenteResponsable = ?
            AND COALESCE(tg.estadoTrabajo, 'Activo') = 'Activo'
            GROUP BY g.idGrupoTGI, tg.idTrabajoGraduacion, g.codigoGrupoTGI, tg.nombreTrabajo
            ORDER BY g.codigoGrupoTGI ASC
            """.trimIndent(),
            arrayOf(internalIdDocente.toString())
        )

        while (cursor.moveToNext()) {
            val idGrupoTGI = getString(cursor, "idGrupoTGI")
            val idTrabajoGraduacion = getString(cursor, "idTrabajoGraduacion")
            Log.d(TAG, "Found group: idGrupoTGI=$idGrupoTGI, idTrabajoGraduacion=$idTrabajoGraduacion")
            grupos.add(
                GrupoInvestigacionDocenteModel(
                    idGrupoTGI = idGrupoTGI,
                    idTrabajoGraduacion = idTrabajoGraduacion,
                    codigoGrupoTGI = getString(cursor, "codigoGrupoTGI"),
                    nombreTrabajo = getString(cursor, "nombreTrabajo"),
                    estadoGrupo = getString(cursor, "estadoGrupo"),
                    totalEstudiantes = obtenerConteoEstudiantesPorTrabajo(idTrabajoGraduacion),
                    totalPropuestas = obtenerConteoPropuestasPorTrabajo(idTrabajoGraduacion),
                    totalDocumentos = obtenerConteoDocumentosPorTrabajo(idTrabajoGraduacion)
                )
            )
        }
        cursor.close()
        Log.d(TAG, "Finished fetching groups. Total found: ${grupos.size}")
        return grupos
    }

    fun obtenerDetalleGrupoInvestigacion(idGrupoTGI: String): GrupoInvestigacionDocenteModel? {
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            """
            SELECT
                g.idGrupoTGI,
                tg.idTrabajoGraduacion,
                g.codigoGrupoTGI,
                tg.nombreTrabajo,
                COALESCE(tg.estadoTrabajo, 'Activo') AS estadoGrupo
            FROM grupo_tgi g
            INNER JOIN trabajo_graduacion tg ON g.idTrabajoGraduacion = tg.idTrabajoGraduacion
            WHERE g.idGrupoTGI = ?
            AND COALESCE(tg.estadoTrabajo, 'Activo') = 'Activo'
            LIMIT 1
            """.trimIndent(),
            arrayOf(idGrupoTGI)
        )

        var grupo: GrupoInvestigacionDocenteModel? = null
        if (cursor.moveToFirst()) {
            val idTrabajoGraduacion = getString(cursor, "idTrabajoGraduacion")
            grupo = GrupoInvestigacionDocenteModel(
                idGrupoTGI = getString(cursor, "idGrupoTGI"),
                idTrabajoGraduacion = idTrabajoGraduacion,
                codigoGrupoTGI = getString(cursor, "codigoGrupoTGI"),
                nombreTrabajo = getString(cursor, "nombreTrabajo"),
                estadoGrupo = getString(cursor, "estadoGrupo"),
                totalEstudiantes = obtenerConteoEstudiantesPorTrabajo(idTrabajoGraduacion),
                totalPropuestas = obtenerConteoPropuestasPorTrabajo(idTrabajoGraduacion),
                totalDocumentos = obtenerConteoDocumentosPorTrabajo(idTrabajoGraduacion)
            )
        }
        cursor.close()
        return grupo
    }

    private fun obtenerConteoEstudiantesPorTrabajo(idTrabajoGraduacion: String): Int {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT COUNT(DISTINCT at.idUsuario) AS totalEstudiantes
            FROM alumno_trabajo at
            WHERE at.idTrabajoGraduacion = ?
            """.trimIndent(),
            arrayOf(idTrabajoGraduacion)
        )
        var count = 0
        if (cursor.moveToFirst()) {
            count = getInt(cursor, "totalEstudiantes")
        }
        cursor.close()
        return count
    }

    private fun obtenerConteoPropuestasPorTrabajo(idTrabajoGraduacion: String): Int {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT COUNT(*) AS totalPropuestas
            FROM propuesta_perfil
            WHERE idTrabajoGraduacion = ?
            """.trimIndent(),
            arrayOf(idTrabajoGraduacion)
        )
        var count = 0
        if (cursor.moveToFirst()) {
            count = getInt(cursor, "totalPropuestas")
        }
        cursor.close()
        return count
    }

    private fun obtenerConteoDocumentosPorTrabajo(idTrabajoGraduacion: String): Int {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT COUNT(*) AS totalDocumentos
            FROM documento
            WHERE idTrabajoGraduacion = ?
            """.trimIndent(),
            arrayOf(idTrabajoGraduacion)
        )
        var count = 0
        if (cursor.moveToFirst()) {
            count = getInt(cursor, "totalDocumentos")
        }
        cursor.close()
        return count
    }

    fun obtenerEstudiantesGrupoInvestigacion(idTrabajoGraduacion: String): List<EstudianteSubgrupoModel> {
        val estudiantes = mutableListOf<EstudianteSubgrupoModel>()
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            """
            SELECT
                u.idUsuario,
                u.carnetUsuario,
                COALESCE(u.primerNombreUsuario, '') || ' ' ||
                COALESCE(u.segundoNombreUsuario, '') || ' ' ||
                COALESCE(u.primerApellidoUsuario, '') || ' ' ||
                COALESCE(u.segundoApellidoUsuario, '') AS nombreCompleto
            FROM alumno_trabajo at
            INNER JOIN usuario u ON at.idUsuario = u.idUsuario
            WHERE at.idTrabajoGraduacion = ?
            AND COALESCE(u.estadoUsuario, 'Activo') = 'Activo'
            ORDER BY u.carnetUsuario ASC
            """.trimIndent(),
            arrayOf(idTrabajoGraduacion)
        )

        while (cursor.moveToNext()) {
            val idUsuario = getString(cursor, "idUsuario")
            estudiantes.add(
                EstudianteSubgrupoModel(
                    idUsuario = idUsuario,
                    carnetUsuario = getString(cursor, "carnetUsuario"),
                    nombreCompleto = getString(cursor, "nombreCompleto"),
                    promedioNotas = String.format("%.2f", obtenerPromedioNotasEstudiante(idUsuario, idTrabajoGraduacion)),
                    idTrabajoGraduacion = idTrabajoGraduacion
                )
            )
        }
        cursor.close()
        return estudiantes
    }

    private fun obtenerPromedioNotasEstudiante(idUsuario: String, idTrabajoGraduacion: String): Double {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT AVG(ne.nota) AS promedioNotas
            FROM nota_etapa ne
            INNER JOIN alumno_trabajo at ON ne.idAlumnoTrabajo = at.idAlumnoTrabajo
            WHERE at.idUsuario = ? AND at.idTrabajoGraduacion = ?
            """.trimIndent(),
            arrayOf(idUsuario, idTrabajoGraduacion)
        )
        var promedio = 0.0
        if (cursor.moveToFirst()) {
            promedio = getDouble(cursor, "promedioNotas") ?: 0.0
        }
        cursor.close()
        return promedio
    }

    fun obtenerPropuestasPerfilGrupoInvestigacion(idTrabajoGraduacion: String): List<PropuestaPerfilDocenteModel> {
        val propuestas = mutableListOf<PropuestaPerfilDocenteModel>()
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            """
            SELECT
                pp.idPropuesta AS idPropuestaPerfil,
                pp.tituloPropuesta,
                pp.urlArchivo AS urlDocumento,
                COALESCE(pp.estadoPropuesta, 'Pendiente') AS estadoPropuesta,
                COALESCE(pp.observacionPropuesta, '') AS observacionPropuesta,
                pp.fechaRegistro
            FROM propuesta_perfil pp
            WHERE pp.idTrabajoGraduacion = ?
            ORDER BY pp.fechaRegistro DESC
            """.trimIndent(),
            arrayOf(idTrabajoGraduacion)
        )

        while (cursor.moveToNext()) {
            propuestas.add(
                PropuestaPerfilDocenteModel(
                    idPropuestaPerfil = getString(cursor, "idPropuestaPerfil") ,
                    tituloPropuesta = getString(cursor, "tituloPropuesta"),
                    urlDocumento = getString(cursor, "urlDocumento"),
                    estadoPropuesta = getString(cursor, "estadoPropuesta"),
                    observacionPropuesta = getString(cursor, "observacionPropuesta"),
                    fechaRegistro = getString(cursor, "fechaRegistro")
                )
            )
        }
        cursor.close()
        return propuestas
    }

    fun obtenerDocumentosGrupoInvestigacion(idTrabajoGraduacion: String): List<DocumentoInvestigacionDocenteModel> {
        val documentos = mutableListOf<DocumentoInvestigacionDocenteModel>()
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
                d.fechaSubida AS fechaCarga
            FROM documento d
            WHERE d.idTrabajoGraduacion = ?
            ORDER BY d.fechaSubida DESC
            """.trimIndent(),
            arrayOf(idTrabajoGraduacion)
        )

        while (cursor.moveToNext()) {
            documentos.add(
                DocumentoInvestigacionDocenteModel(
                    idDocumento = getString(cursor, "idDocumento"),
                    nombreDocumento = getString(cursor, "nombreDocumento"),
                    tipoDocumento = getString(cursor, "tipoDocumento"),
                    versionDocumento = getInt(cursor, "versionDocumento"),
                    urlDocumento = getString(cursor, "urlDocumento"),
                    estadoDocumento = getString(cursor, "estadoDocumento"),
                    observacionDocumento = getString(cursor, "observacionDocumento"),
                    fechaCarga = getString(cursor, "fechaCarga")
                )
            )
        }
        cursor.close()
        return documentos
    }

    fun obtenerNotasGrupoInvestigacion(idTrabajoGraduacion: String): List<NotaEtapaDocenteModel> {
        val notas = mutableListOf<NotaEtapaDocenteModel>()
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            """
            SELECT
                ne.idNotaEtapa,
                at.idTrabajoGraduacion,
                at.idUsuario,
                ne.numeroEtapa AS idEtapaEvaluacion,
                ne.nota,
                ne.observacionNota AS observacion,
                ne.fechaRegistro,
                u.carnetUsuario
            FROM nota_etapa ne
            INNER JOIN alumno_trabajo at ON ne.idAlumnoTrabajo = at.idAlumnoTrabajo
            INNER JOIN usuario u ON at.idUsuario = u.idUsuario
            WHERE at.idTrabajoGraduacion = ?
            ORDER BY u.carnetUsuario ASC, ne.fechaRegistro ASC
            """.trimIndent(),
            arrayOf(idTrabajoGraduacion)
        )

        while (cursor.moveToNext()) {
            val idEtapaEvaluacion = getInt(cursor, "idEtapaEvaluacion")
            val nombreEtapa = STAGE_ID_TO_NAME["ETAPA_$idEtapaEvaluacion"] ?: "Etapa $idEtapaEvaluacion"
            notas.add(
                NotaEtapaDocenteModel(
                    idNotaEtapa = getString(cursor, "idNotaEtapa"),
                    idTrabajoGraduacion = getString(cursor, "idTrabajoGraduacion"),
                    idUsuario = getString(cursor, "idUsuario"),
                    nombreEtapa = "${nombreEtapa} (${getString(cursor, "carnetUsuario")})", // Include student carnet
                    nota = getDouble(cursor, "nota"),
                    observacion = getString(cursor, "observacion"),
                    fechaRegistro = getString(cursor, "fechaRegistro")
                )
            )
        }
        cursor.close()
        return notas
    }
}