// CursoEspecializacionDocenteDao.kt
package com.example.gradues.data.dao

import android.content.ContentValues
import android.database.Cursor
import android.util.Log
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.CursoEspecializacionDocenteModel
import com.example.gradues.data.model.EstudianteSubgrupoModel
import com.example.gradues.data.model.NotaEtapaDocenteModel
import com.example.gradues.data.model.PropuestaEspecializacionDocenteModel
import com.example.gradues.data.model.SubgrupoEspecializacionDocenteModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CursoEspecializacionDocenteDao(private val dbHelper: DatabaseHelper) {

    companion object {
        private const val TAG = "CursoEspecializacionDocenteDao"

        // Define constant stage names and their "IDs" if `etapa_evaluacion` table is not available.
        // Assuming 4 stages as requested by "Use 4 stages as a constant if no config table exists."
        const val STAGE_1_ID = "ETAPA_1"
        const val STAGE_1_NAME = "Entrega de Perfil"
        const val STAGE_2_ID = "ETAPA_2"
        const val STAGE_2_NAME = "Avance 1"
        const val STAGE_3_ID = "ETAPA_3"
        const val STAGE_3_NAME = "Avance 2"
        const val STAGE_4_ID = "ETAPA_4"
        const val STAGE_4_NAME = "Entrega Final"

        val STAGE_NAME_TO_ID = mapOf(
            STAGE_1_NAME to STAGE_1_ID,
            STAGE_2_NAME to STAGE_2_ID,
            STAGE_3_NAME to STAGE_3_ID,
            STAGE_4_NAME to STAGE_4_ID
        )

        val STAGE_ID_TO_NAME = mapOf(
            STAGE_1_ID to STAGE_1_NAME,
            STAGE_2_ID to STAGE_2_NAME,
            STAGE_3_ID to STAGE_3_NAME,
            STAGE_4_ID to STAGE_4_NAME
        )
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

    fun obtenerCursosEspecializacionAsignados(idDocente: String): List<CursoEspecializacionDocenteModel> {
        val cursos = mutableListOf<CursoEspecializacionDocenteModel>()
        val db = dbHelper.readableDatabase

        val internalIdDocente = getInternalUserId(idDocente)
        if (internalIdDocente == null) {
            Log.d(TAG, "Internal ID for docente $idDocente not found.")
            return emptyList()
        }

        // Query to get distinct specialization courses assigned to the teacher
        val cursor = db.rawQuery(
            """
            SELECT
                gtge.idGrupoTGE,
                gtge.nombreCurso,
                gtge.cicloAcademico AS cicloCurso,
                COUNT(DISTINCT sg.idSubgrupoTGE) AS totalSubgrupos
            FROM grupo_tge gtge
            INNER JOIN subgrupo_tge sg ON gtge.idGrupoTGE = sg.idGrupoTGE
            INNER JOIN trabajo_graduacion tg ON sg.idTrabajoGraduacion = tg.idTrabajoGraduacion
            WHERE tg.idDocenteResponsable = ?
            GROUP BY gtge.idGrupoTGE, gtge.nombreCurso, gtge.cicloAcademico
            ORDER BY gtge.nombreCurso ASC
            """.trimIndent(),
            arrayOf(internalIdDocente.toString())
        )

        while (cursor.moveToNext()) {
            val idGrupoTGE = getString(cursor, "idGrupoTGE")
            val totalSubgrupos = getInt(cursor, "totalSubgrupos")
            val totalEstudiantes = obtenerConteoEstudiantesPorCurso(idGrupoTGE) // Additional query for students per course

            cursos.add(
                CursoEspecializacionDocenteModel(
                    idGrupoTGE = idGrupoTGE,
                    nombreCurso = getString(cursor, "nombreCurso"),
                    ciclo = getString(cursor, "cicloCurso"),
                    totalSubgrupos = totalSubgrupos,
                    totalEstudiantes = totalEstudiantes
                )
            )
        }
        cursor.close()
        return cursos
    }

    private fun obtenerConteoEstudiantesPorCurso(idGrupoTGE: String): Int {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT COUNT(DISTINCT at.idUsuario) AS totalEstudiantes
            FROM grupo_tge gtge
            INNER JOIN subgrupo_tge sg ON gtge.idGrupoTGE = sg.idGrupoTGE
            INNER JOIN trabajo_graduacion tg ON sg.idTrabajoGraduacion = tg.idTrabajoGraduacion
            INNER JOIN alumno_trabajo at ON tg.idTrabajoGraduacion = at.idTrabajoGraduacion
            WHERE gtge.idGrupoTGE = ?
            AND COALESCE(tg.estadoTrabajo, 'Activo') = 'Activo'
            """.trimIndent(),
            arrayOf(idGrupoTGE)
        )
        var count = 0
        if (cursor.moveToFirst()) {
            count = getInt(cursor, "totalEstudiantes")
        }
        cursor.close()
        return count
    }


    fun obtenerDetalleCursoEspecializacion(idGrupoTGE: String): CursoEspecializacionDocenteModel? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT
                gtge.idGrupoTGE,
                gtge.nombreCurso,
                gtge.cicloAcademico AS cicloCurso
            FROM grupo_tge gtge
            WHERE gtge.idGrupoTGE = ?
            LIMIT 1
            """.trimIndent(),
            arrayOf(idGrupoTGE)
        )

        var curso: CursoEspecializacionDocenteModel? = null
        if (cursor.moveToFirst()) {
            val totalSubgrupos = obtenerConteoSubgruposPorCurso(idGrupoTGE)
            val totalEstudiantes = obtenerConteoEstudiantesPorCurso(idGrupoTGE)

            curso = CursoEspecializacionDocenteModel(
                idGrupoTGE = getString(cursor, "idGrupoTGE"),
                nombreCurso = getString(cursor, "nombreCurso"),
                ciclo = getString(cursor, "cicloCurso"),
                totalSubgrupos = totalSubgrupos,
                totalEstudiantes = totalEstudiantes
            )
        }
        cursor.close()
        return curso
    }

    private fun obtenerConteoSubgruposPorCurso(idGrupoTGE: String): Int {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT COUNT(*) AS totalSubgrupos
            FROM subgrupo_tge
            WHERE idGrupoTGE = ?
            """.trimIndent(),
            arrayOf(idGrupoTGE)
        )
        var count = 0
        if (cursor.moveToFirst()) {
            count = getInt(cursor, "totalSubgrupos")
        }
        cursor.close()
        return count
    }

    fun obtenerSubgruposPorCurso(idGrupoTGE: String, idDocente: String): List<SubgrupoEspecializacionDocenteModel> {
        val subgrupos = mutableListOf<SubgrupoEspecializacionDocenteModel>()
        val db = dbHelper.readableDatabase

        val internalIdDocente = getInternalUserId(idDocente)
        if (internalIdDocente == null) {
            Log.d(TAG, "Internal ID for docente $idDocente not found.")
            return emptyList()
        }

        val cursor = db.rawQuery(
            """
            SELECT
                sg.idSubgrupoTGE,
                tg.idTrabajoGraduacion,
                sg.nombreSubgrupo,
                tg.nombreTrabajo AS temaAsignado,
                'Activo' AS estadoSubgrupo
            FROM subgrupo_tge sg
            INNER JOIN trabajo_graduacion tg ON sg.idTrabajoGraduacion = tg.idTrabajoGraduacion
            WHERE sg.idGrupoTGE = ?
            AND tg.idDocenteResponsable = ?
            AND COALESCE(tg.estadoTrabajo, 'Activo') = 'Activo'
            ORDER BY sg.nombreSubgrupo ASC
            """.trimIndent(),
            arrayOf(idGrupoTGE, internalIdDocente.toString())
        )

        while (cursor.moveToNext()) {
            val idSubgrupoTGE = getString(cursor, "idSubgrupoTGE")
            val idTrabajoGraduacion = getString(cursor, "idTrabajoGraduacion")
            val totalEstudiantes = obtenerConteoEstudiantesPorSubgrupo(idSubgrupoTGE)
            val promedioGeneral = String.format(Locale.US, "%.2f", obtenerPromedioGeneralSubgrupo(idTrabajoGraduacion))

            subgrupos.add(
                SubgrupoEspecializacionDocenteModel(
                    idSubgrupoTGE = idSubgrupoTGE,
                    idTrabajoGraduacion = idTrabajoGraduacion,
                    nombreSubgrupo = getString(cursor, "nombreSubgrupo"),
                    temaAsignado = getString(cursor, "temaAsignado"),
                    estadoSubgrupo = getString(cursor, "estadoSubgrupo"),
                    totalEstudiantes = totalEstudiantes,
                    promedioGeneralSubgrupo = promedioGeneral
                )
            )
        }
        cursor.close()
        return subgrupos
    }

    private fun obtenerConteoEstudiantesPorSubgrupo(idSubgrupoTGE: String): Int {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT COUNT(DISTINCT at.idUsuario) AS totalEstudiantes
            FROM subgrupo_tge sg
            INNER JOIN trabajo_graduacion tg ON sg.idTrabajoGraduacion = tg.idTrabajoGraduacion
            INNER JOIN alumno_trabajo at ON tg.idTrabajoGraduacion = at.idTrabajoGraduacion
            WHERE sg.idSubgrupoTGE = ?
            AND COALESCE(tg.estadoTrabajo, 'Activo') = 'Activo'
            """.trimIndent(),
            arrayOf(idSubgrupoTGE)
        )
        var count = 0
        if (cursor.moveToFirst()) {
            count = getInt(cursor, "totalEstudiantes")
        }
        cursor.close()
        return count
    }

    private fun obtenerPromedioGeneralSubgrupo(idTrabajoGraduacion: String): Double {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT AVG(ne.nota) AS promedio
            FROM nota_etapa ne
            INNER JOIN alumno_trabajo at ON ne.idAlumnoTrabajo = at.idAlumnoTrabajo
            WHERE at.idTrabajoGraduacion = ?
            """.trimIndent(),
            arrayOf(idTrabajoGraduacion)
        )
        var promedio = 0.0
        if (cursor.moveToFirst()) {
            promedio = getDouble(cursor, "promedio") ?: 0.0
        }
        cursor.close()
        return promedio
    }

    fun obtenerEstudiantesPorSubgrupo(idTrabajoGraduacion: String): List<EstudianteSubgrupoModel> {
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
            val promedioNotas = String.format(Locale.US, "%.2f", obtenerPromedioNotasEstudiante(idUsuario, idTrabajoGraduacion))
            estudiantes.add(
                EstudianteSubgrupoModel(
                    idUsuario = idUsuario,
                    carnetUsuario = getString(cursor, "carnetUsuario"),
                    nombreCompleto = getString(cursor, "nombreCompleto"),
                    promedioNotas = promedioNotas,
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

    fun obtenerNotasEstudiante(idUsuario: String, idTrabajoGraduacion: String): List<NotaEtapaDocenteModel> {
        val notas = mutableListOf<NotaEtapaDocenteModel>()
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            """
            SELECT
                ne.idNotaEtapa,
                at.idTrabajoGraduacion,
                at.idUsuario,
                ne.numeroEtapa AS idEtapaEvaluacion, -- Get the raw stage number
                ne.nota,
                ne.observacionNota AS observacion,
                ne.fechaRegistro
            FROM nota_etapa ne
            INNER JOIN alumno_trabajo at ON ne.idAlumnoTrabajo = at.idAlumnoTrabajo
            WHERE at.idUsuario = ? AND at.idTrabajoGraduacion = ?
            ORDER BY ne.fechaRegistro ASC
            """.trimIndent(),
            arrayOf(idUsuario, idTrabajoGraduacion)
        )

        while (cursor.moveToNext()) {
            val rawEtapaVal = getString(cursor, "idEtapaEvaluacion")
            val idEtapaEvaluacion = "ETAPA_$rawEtapaVal"
            val nombreEtapa = STAGE_ID_TO_NAME[idEtapaEvaluacion] ?: "Etapa $rawEtapaVal" // Map ID to name

            notas.add(
                NotaEtapaDocenteModel(
                    idNotaEtapa = getString(cursor, "idNotaEtapa"),
                    idTrabajoGraduacion = getString(cursor, "idTrabajoGraduacion"),
                    idUsuario = getString(cursor, "idUsuario"),
                    nombreEtapa = nombreEtapa, // Use mapped name
                    nota = getDouble(cursor, "nota"),
                    observacion = getString(cursor, "observacion"),
                    fechaRegistro = getString(cursor, "fechaRegistro")
                )
            )
        }
        cursor.close()
        return notas
    }

    fun obtenerPropuestasEspecializacion(idTrabajoGraduacion: String): List<PropuestaEspecializacionDocenteModel> {
        val propuestas = mutableListOf<PropuestaEspecializacionDocenteModel>()
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            """
            SELECT
                pp.idPropuesta,
                pp.idTrabajoGraduacion,
                COALESCE(pp.tituloPropuesta, 'Sin título') AS tituloPropuesta,
                COALESCE(pp.descripcionPropuesta, 'Sin descripción') AS descripcionPropuesta,
                COALESCE(pp.estadoPropuesta, 'Pendiente') AS estadoPropuesta,
                pp.observacionPropuesta,
                pp.urlArchivo,
                COALESCE(pp.fechaRegistro, '') AS fechaRegistro,
                COALESCE(sg.nombreSubgrupo, 'Subgrupo') AS nombreSubgrupo,
                COALESCE(gtge.nombreCurso, 'Curso de especialización') AS nombreCurso
            FROM propuesta_perfil pp
            INNER JOIN trabajo_graduacion tg
                ON tg.idTrabajoGraduacion = pp.idTrabajoGraduacion
            LEFT JOIN subgrupo_tge sg
                ON sg.idTrabajoGraduacion = tg.idTrabajoGraduacion
            LEFT JOIN grupo_tge gtge
                ON gtge.idGrupoTGE = sg.idGrupoTGE
            WHERE pp.idTrabajoGraduacion = ?
            ORDER BY pp.fechaRegistro DESC, pp.idPropuesta DESC
            """.trimIndent(),
            arrayOf(idTrabajoGraduacion)
        )

        while (cursor.moveToNext()) {
            propuestas.add(
                PropuestaEspecializacionDocenteModel(
                    idPropuesta = getInt(cursor, "idPropuesta"),
                    idTrabajoGraduacion = getInt(cursor, "idTrabajoGraduacion"),
                    tituloPropuesta = getString(cursor, "tituloPropuesta"),
                    descripcionPropuesta = getString(cursor, "descripcionPropuesta"),
                    estadoPropuesta = getString(cursor, "estadoPropuesta"),
                    observacionPropuesta = getString(cursor, "observacionPropuesta").ifBlank { null },
                    urlArchivo = getString(cursor, "urlArchivo").ifBlank { null },
                    fechaRegistro = getString(cursor, "fechaRegistro"),
                    nombreSubgrupo = getString(cursor, "nombreSubgrupo"),
                    nombreCurso = getString(cursor, "nombreCurso")
                )
            )
        }

        cursor.close()
        return propuestas
    }

    fun actualizarRevisionPropuestaEspecializacion(
        idPropuesta: Int,
        estadoPropuesta: String,
        observacionPropuesta: String?
    ): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("estadoPropuesta", estadoPropuesta)
            put("observacionPropuesta", observacionPropuesta)
        }

        return db.update(
            "propuesta_perfil",
            values,
            "idPropuesta = ?",
            arrayOf(idPropuesta.toString())
        ) > 0
    }

    private fun obtenerIdAlumnoTrabajo(idUsuario: String, idTrabajoGraduacion: String): Int? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT idAlumnoTrabajo FROM alumno_trabajo WHERE idUsuario = ? AND idTrabajoGraduacion = ? LIMIT 1",
            arrayOf(idUsuario, idTrabajoGraduacion)
        )
        var id: Int? = null
        if (cursor.moveToFirst()) {
            id = cursor.getInt(cursor.getColumnIndexOrThrow("idAlumnoTrabajo"))
        }
        cursor.close()
        return id
    }

    fun crearOActualizarNotaConEtapaNombre(
        idNotaEtapa: String?,
        idTrabajoGraduacion: String,
        idUsuario: String,
        nombreEtapa: String,
        nota: Double,
        observacion: String?
    ): Boolean {
        val db = dbHelper.writableDatabase

        val idAlumnoTrabajo = obtenerIdAlumnoTrabajo(idUsuario, idTrabajoGraduacion) ?: return false
        val idEtapaEvaluacion = STAGE_NAME_TO_ID[nombreEtapa] ?: return false
        val numeroEtapa = idEtapaEvaluacion.removePrefix("ETAPA_").toIntOrNull() ?: return false

        val values = ContentValues().apply {
            put("idAlumnoTrabajo", idAlumnoTrabajo)
            put("numeroEtapa", numeroEtapa)
            put("nota", nota)
            put("observacionNota", observacion)
            put("fechaRegistro", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()))
        }

        return if (idNotaEtapa.isNullOrBlank()) {
            // Insert new note
            db.insert("nota_etapa", null, values) > 0
        } else {
            // Update existing note
            db.update("nota_etapa", values, "idNotaEtapa = ?", arrayOf(idNotaEtapa)) > 0
        }
    }
}
