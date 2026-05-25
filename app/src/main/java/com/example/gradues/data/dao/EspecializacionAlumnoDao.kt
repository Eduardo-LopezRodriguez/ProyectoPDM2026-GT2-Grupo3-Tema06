package com.example.gradues.data.dao

import android.database.Cursor
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.DetalleSubgrupoEspecializacionAlumnoModel
import com.example.gradues.data.model.DocumentoTesinaAlumnoModel
import com.example.gradues.data.model.IntegranteSubgrupoAlumnoModel
import com.example.gradues.data.model.JuradoEspecializacionAlumnoModel
import com.example.gradues.data.model.NotaAlumnoModel
import com.example.gradues.data.model.ResumenPropuestasAlumnoModel

class EspecializacionAlumnoDao(
    private val dbHelper: DatabaseHelper
) {

    fun obtenerDetalleSubgrupoActivo(idSesion: String): DetalleSubgrupoEspecializacionAlumnoModel? {
        val trabajo = obtenerTrabajoEspecializacionActivo(idSesion) ?: return null

        return DetalleSubgrupoEspecializacionAlumnoModel(
            idTrabajoGraduacion = trabajo.idTrabajoGraduacion,
            idAlumnoTrabajo = trabajo.idAlumnoTrabajo,
            nombreSubgrupo = trabajo.nombreSubgrupo,
            nombreCurso = trabajo.nombreCurso,
            temaAsignado = trabajo.temaAsignado,
            director = obtenerDirector(trabajo.idTrabajoGraduacion),
            integrantes = obtenerIntegrantes(trabajo.idTrabajoGraduacion),
            jurados = obtenerJurados(trabajo.idTrabajoGraduacion),
            resumenPropuestas = obtenerResumenPropuestas(trabajo.idTrabajoGraduacion),
            tesina = obtenerTesina(trabajo.idTrabajoGraduacion),
            notas = obtenerNotasAlumno(trabajo.idAlumnoTrabajo),
            promedioAlumno = obtenerPromedioAlumno(trabajo.idAlumnoTrabajo),
            promedioGrupo = obtenerPromedioGrupo(trabajo.idTrabajoGraduacion)
        )
    }

    private fun obtenerTrabajoEspecializacionActivo(idSesion: String): TrabajoEspecializacionActivo? {
        val db = dbHelper.readableDatabase
        val datoSesion = idSesion.trim()

        val cursor = db.rawQuery(
            """
            SELECT
                at.idAlumnoTrabajo,
                tg.idTrabajoGraduacion,
                COALESCE(sgtge.nombreSubgrupo, 'Subgrupo sin nombre') AS nombreSubgrupo,
                COALESCE(gte.nombreCurso, tg.nombreTrabajo, 'Curso sin nombre') AS nombreCurso,
                COALESCE(NULLIF(TRIM(sgtge.temaAsignado), ''), tg.nombreTrabajo, 'Tema sin asignar') AS temaAsignado
            FROM usuario u
            INNER JOIN alumno_trabajo at
                ON at.idUsuario = u.idUsuario
            INNER JOIN trabajo_graduacion tg
                ON tg.idTrabajoGraduacion = at.idTrabajoGraduacion
            INNER JOIN modalidad m
                ON m.idModalidad = tg.idModalidad
            INNER JOIN subgrupo_tge sgtge
                ON sgtge.idTrabajoGraduacion = tg.idTrabajoGraduacion
            INNER JOIN grupo_tge gte
                ON gte.idGrupoTGE = sgtge.idGrupoTGE
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
            TrabajoEspecializacionActivo(
                idAlumnoTrabajo = cursor.getInt(cursor.getColumnIndexOrThrow("idAlumnoTrabajo")),
                idTrabajoGraduacion = cursor.getInt(cursor.getColumnIndexOrThrow("idTrabajoGraduacion")),
                nombreSubgrupo = getString(cursor, "nombreSubgrupo"),
                nombreCurso = getString(cursor, "nombreCurso"),
                temaAsignado = getString(cursor, "temaAsignado")
            )
        } else {
            null
        }

        cursor.close()
        return trabajo
    }

    private fun obtenerDirector(idTrabajoGraduacion: Int): IntegranteSubgrupoAlumnoModel? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT
                u.idUsuario,
                COALESCE(NULLIF(TRIM(u.nombreUsuario), ''), 'Docente sin nombre') AS nombreCompleto,
                COALESCE(NULLIF(TRIM(u.carnetUsuario), ''), NULLIF(TRIM(u.correoUsuario), ''), CAST(u.idUsuario AS TEXT)) AS codigoUsuario
            FROM trabajo_graduacion tg
            INNER JOIN usuario u
                ON u.idUsuario = tg.idDocenteResponsable
            WHERE tg.idTrabajoGraduacion = ?
            LIMIT 1
            """.trimIndent(),
            arrayOf(idTrabajoGraduacion.toString())
        )

        val director = if (cursor.moveToFirst()) {
            IntegranteSubgrupoAlumnoModel(
                idUsuario = cursor.getInt(cursor.getColumnIndexOrThrow("idUsuario")),
                nombreCompleto = getString(cursor, "nombreCompleto"),
                carnetUsuario = getString(cursor, "codigoUsuario")
            )
        } else {
            null
        }

        cursor.close()
        return director
    }

    private fun obtenerIntegrantes(idTrabajoGraduacion: Int): List<IntegranteSubgrupoAlumnoModel> {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT
                u.idUsuario,
                COALESCE(NULLIF(TRIM(u.nombreUsuario), ''), 'Estudiante sin nombre') AS nombreCompleto,
                COALESCE(NULLIF(TRIM(u.carnetUsuario), ''), CAST(u.idUsuario AS TEXT)) AS carnetUsuario
            FROM alumno_trabajo at
            INNER JOIN usuario u
                ON u.idUsuario = at.idUsuario
            WHERE at.idTrabajoGraduacion = ?
              AND UPPER(COALESCE(at.estadoAlumnoTrabajo, 'ACTIVO')) = 'ACTIVO'
            ORDER BY u.nombreUsuario ASC, u.carnetUsuario ASC
            """.trimIndent(),
            arrayOf(idTrabajoGraduacion.toString())
        )

        val integrantes = mutableListOf<IntegranteSubgrupoAlumnoModel>()
        while (cursor.moveToNext()) {
            integrantes.add(
                IntegranteSubgrupoAlumnoModel(
                    idUsuario = cursor.getInt(cursor.getColumnIndexOrThrow("idUsuario")),
                    nombreCompleto = getString(cursor, "nombreCompleto"),
                    carnetUsuario = getString(cursor, "carnetUsuario")
                )
            )
        }

        cursor.close()
        return integrantes
    }

    private fun obtenerJurados(idTrabajoGraduacion: Int): List<JuradoEspecializacionAlumnoModel> {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT
                u.idUsuario,
                COALESCE(NULLIF(TRIM(u.nombreUsuario), ''), 'Jurado sin nombre') AS nombreCompleto,
                COALESCE(NULLIF(TRIM(u.carnetUsuario), ''), NULLIF(TRIM(u.correoUsuario), ''), CAST(u.idUsuario AS TEXT)) AS codigoUsuario,
                COALESCE(aj.fechaAsignacion, '') AS fechaAsignacion
            FROM asignacion_jurado aj
            INNER JOIN usuario u
                ON u.idUsuario = aj.idUsuario
            WHERE aj.idTrabajoGraduacion = ?
            ORDER BY u.nombreUsuario ASC
            """.trimIndent(),
            arrayOf(idTrabajoGraduacion.toString())
        )

        val jurados = mutableListOf<JuradoEspecializacionAlumnoModel>()
        while (cursor.moveToNext()) {
            jurados.add(
                JuradoEspecializacionAlumnoModel(
                    idUsuario = cursor.getInt(cursor.getColumnIndexOrThrow("idUsuario")),
                    nombreCompleto = getString(cursor, "nombreCompleto"),
                    codigoUsuario = getString(cursor, "codigoUsuario"),
                    fechaAsignacion = getString(cursor, "fechaAsignacion")
                )
            )
        }

        cursor.close()
        return jurados
    }

    private fun obtenerResumenPropuestas(idTrabajoGraduacion: Int): ResumenPropuestasAlumnoModel {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT
                COUNT(*) AS total,
                SUM(CASE WHEN UPPER(COALESCE(estadoPropuesta, '')) = 'PENDIENTE' THEN 1 ELSE 0 END) AS pendientes,
                SUM(CASE WHEN UPPER(COALESCE(estadoPropuesta, '')) = 'APROBADA' THEN 1 ELSE 0 END) AS aprobadas,
                SUM(CASE WHEN UPPER(COALESCE(estadoPropuesta, '')) = 'DENEGADA' THEN 1 ELSE 0 END) AS denegadas,
                SUM(CASE WHEN UPPER(COALESCE(estadoPropuesta, '')) = 'BORRADOR' THEN 1 ELSE 0 END) AS borradores,
                SUM(CASE WHEN UPPER(COALESCE(estadoPropuesta, '')) = 'SELECCIONADA' THEN 1 ELSE 0 END) AS seleccionadas,
                SUM(CASE WHEN UPPER(COALESCE(estadoPropuesta, '')) = 'DESCARTADA' THEN 1 ELSE 0 END) AS descartadas,
                SUM(CASE WHEN UPPER(COALESCE(estadoPropuesta, '')) = 'CON OBSERVACIÓN'
                           OR UPPER(COALESCE(estadoPropuesta, '')) = 'CON OBSERVACION'
                         THEN 1 ELSE 0 END) AS conObservacion
            FROM propuesta_perfil
            WHERE idTrabajoGraduacion = ?
            """.trimIndent(),
            arrayOf(idTrabajoGraduacion.toString())
        )

        val resumen = if (cursor.moveToFirst()) {
            ResumenPropuestasAlumnoModel(
                total = getInt(cursor, "total"),
                pendientes = getInt(cursor, "pendientes"),
                aprobadas = getInt(cursor, "aprobadas"),
                denegadas = getInt(cursor, "denegadas"),
                borradores = getInt(cursor, "borradores"),
                seleccionadas = getInt(cursor, "seleccionadas"),
                descartadas = getInt(cursor, "descartadas"),
                conObservacion = getInt(cursor, "conObservacion")
            )
        } else {
            ResumenPropuestasAlumnoModel(0, 0, 0, 0, 0, 0, 0, 0)
        }

        cursor.close()
        return resumen
    }

    private fun obtenerTesina(idTrabajoGraduacion: Int): DocumentoTesinaAlumnoModel? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT
                COALESCE(NULLIF(TRIM(tituloDocumento), ''), 'Tesina del grupo') AS tituloDocumento,
                COALESCE(NULLIF(TRIM(estadoDocumento), ''), 'No subida') AS estadoDocumento,
                versionDocumento,
                observacionDocumento,
                fechaSubida
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

        val tesina = if (cursor.moveToFirst()) {
            DocumentoTesinaAlumnoModel(
                tituloDocumento = getString(cursor, "tituloDocumento"),
                estadoDocumento = getString(cursor, "estadoDocumento"),
                versionDocumento = getNullableInt(cursor, "versionDocumento"),
                observacionDocumento = getNullableString(cursor, "observacionDocumento"),
                fechaSubida = getNullableString(cursor, "fechaSubida")
            )
        } else {
            null
        }

        cursor.close()
        return tesina
    }

    private fun obtenerNotasAlumno(idAlumnoTrabajo: Int): List<NotaAlumnoModel> {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT numeroEtapa, nota, observacionNota, fechaRegistro
            FROM nota_etapa
            WHERE idAlumnoTrabajo = ?
            ORDER BY numeroEtapa ASC, idNotaEtapa ASC
            """.trimIndent(),
            arrayOf(idAlumnoTrabajo.toString())
        )

        val notas = mutableListOf<NotaAlumnoModel>()
        while (cursor.moveToNext()) {
            notas.add(
                NotaAlumnoModel(
                    numeroEtapa = cursor.getInt(cursor.getColumnIndexOrThrow("numeroEtapa")),
                    nota = getNullableDouble(cursor, "nota"),
                    observacion = getNullableString(cursor, "observacionNota"),
                    fechaRegistro = getNullableString(cursor, "fechaRegistro")
                )
            )
        }

        cursor.close()
        return notas
    }

    private fun obtenerPromedioAlumno(idAlumnoTrabajo: Int): Double? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT (
                COALESCE(MAX(CASE WHEN numeroEtapa = 1 THEN nota END), 0) +
                COALESCE(MAX(CASE WHEN numeroEtapa = 2 THEN nota END), 0) +
                COALESCE(MAX(CASE WHEN numeroEtapa = 3 THEN nota END), 0) +
                COALESCE(MAX(CASE WHEN numeroEtapa = 4 THEN nota END), 0)
            ) / 4.0 AS promedio
            FROM nota_etapa
            WHERE idAlumnoTrabajo = ?
            """.trimIndent(),
            arrayOf(idAlumnoTrabajo.toString())
        )

        val promedio = if (cursor.moveToFirst()) getNullableDouble(cursor, "promedio") else null
        cursor.close()
        return promedio
    }

    private fun obtenerPromedioGrupo(idTrabajoGraduacion: Int): Double? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT AVG(promedioAlumno) AS promedio
            FROM (
                SELECT
                    at.idAlumnoTrabajo,
                    (
                        COALESCE(MAX(CASE WHEN ne.numeroEtapa = 1 THEN ne.nota END), 0) +
                        COALESCE(MAX(CASE WHEN ne.numeroEtapa = 2 THEN ne.nota END), 0) +
                        COALESCE(MAX(CASE WHEN ne.numeroEtapa = 3 THEN ne.nota END), 0) +
                        COALESCE(MAX(CASE WHEN ne.numeroEtapa = 4 THEN ne.nota END), 0)
                    ) / 4.0 AS promedioAlumno
                FROM alumno_trabajo at
                LEFT JOIN nota_etapa ne
                    ON ne.idAlumnoTrabajo = at.idAlumnoTrabajo
                WHERE at.idTrabajoGraduacion = ?
                  AND UPPER(COALESCE(at.estadoAlumnoTrabajo, 'ACTIVO')) = 'ACTIVO'
                GROUP BY at.idAlumnoTrabajo
            )
            """.trimIndent(),
            arrayOf(idTrabajoGraduacion.toString())
        )

        val promedio = if (cursor.moveToFirst()) getNullableDouble(cursor, "promedio") else null
        cursor.close()
        return promedio
    }

    private fun getString(cursor: Cursor, column: String): String {
        val index = cursor.getColumnIndexOrThrow(column)
        return if (cursor.isNull(index)) "" else cursor.getString(index)
    }

    private fun getNullableString(cursor: Cursor, column: String): String? {
        val index = cursor.getColumnIndexOrThrow(column)
        return if (cursor.isNull(index)) null else cursor.getString(index)
    }

    private fun getInt(cursor: Cursor, column: String): Int {
        val index = cursor.getColumnIndexOrThrow(column)
        return if (cursor.isNull(index)) 0 else cursor.getInt(index)
    }

    private fun getNullableInt(cursor: Cursor, column: String): Int? {
        val index = cursor.getColumnIndexOrThrow(column)
        return if (cursor.isNull(index)) null else cursor.getInt(index)
    }

    private fun getNullableDouble(cursor: Cursor, column: String): Double? {
        val index = cursor.getColumnIndexOrThrow(column)
        return if (cursor.isNull(index)) null else cursor.getDouble(index)
    }

    private data class TrabajoEspecializacionActivo(
        val idAlumnoTrabajo: Int,
        val idTrabajoGraduacion: Int,
        val nombreSubgrupo: String,
        val nombreCurso: String,
        val temaAsignado: String
    )
}
