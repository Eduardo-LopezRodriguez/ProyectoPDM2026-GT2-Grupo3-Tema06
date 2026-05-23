package com.example.gradues.data.dao

import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.DashboardAlumnoModel

class DashboardAlumnoDao(private val dbHelper: DatabaseHelper) {

    fun obtenerResumenAlumno(idSesion: String): DashboardAlumnoModel? {
        val db = dbHelper.readableDatabase

        val query = """
            SELECT
                TRIM(
                    COALESCE(u.primerNombreUsuario, '') || ' ' ||
                    COALESCE(u.segundoNombreUsuario, '') || ' ' ||
                    COALESCE(u.primerApellidoUsuario, '') || ' ' ||
                    COALESCE(u.segundoApellidoUsuario, '')
                ) AS nombreCompleto,
                COALESCE(u.carnetUsuario, CAST(u.idUsuario AS TEXT)) AS carnet,
                COALESCE(m.tipoModalidad, 'Sin modalidad') AS modalidadActual,
                COALESCE(sm.estadoSolicitud, 'Sin solicitud') AS estadoSolicitud,
                COALESCE(tg.nombreTrabajo, 'Sin trabajo asignado') AS nombreTrabajo,
                COALESCE(
                    gtgi.codigoGrupoTGI,
                    sm.codigoAgrupacionSolicitud,
                    sgtge.nombreSubgrupo,
                    'Sin código'
                ) AS codigoAgrupacion,
                (
                    SELECT COUNT(*)
                    FROM documento d
                    WHERE d.idTrabajoGraduacion = tg.idTrabajoGraduacion
                ) AS totalDocumentos,
                (
                    SELECT COUNT(*)
                    FROM propuesta_perfil pp
                    WHERE pp.idTrabajoGraduacion = tg.idTrabajoGraduacion
                ) AS totalPropuestas,
                (
                    SELECT COUNT(ne.idNotaEtapa)
                    FROM nota_etapa ne
                    INNER JOIN alumno_trabajo at2
                        ON at2.idAlumnoTrabajo = ne.idAlumnoTrabajo
                    WHERE at2.idUsuario = u.idUsuario
                      AND ne.nota IS NOT NULL
                ) AS totalNotasRegistradas,
                COALESCE((
                    SELECT AVG(ne.nota)
                    FROM nota_etapa ne
                    INNER JOIN alumno_trabajo at3
                        ON at3.idAlumnoTrabajo = ne.idAlumnoTrabajo
                    WHERE at3.idUsuario = u.idUsuario
                      AND ne.nota IS NOT NULL
                ), 0.0) AS promedioNotas
            FROM usuario u
            LEFT JOIN solicitud_modalidad sm
                ON sm.idUsuario = u.idUsuario
                AND sm.idSolicitudModalidad = (
                    SELECT MAX(sm2.idSolicitudModalidad)
                    FROM solicitud_modalidad sm2
                    WHERE sm2.idUsuario = u.idUsuario
                )
            LEFT JOIN modalidad m
                ON m.idModalidad = sm.idModalidad
            LEFT JOIN alumno_trabajo at
                ON at.idUsuario = u.idUsuario
            LEFT JOIN trabajo_graduacion tg
                ON tg.idTrabajoGraduacion = at.idTrabajoGraduacion
            LEFT JOIN grupo_tgi gtgi
                ON gtgi.idTrabajoGraduacion = tg.idTrabajoGraduacion
            LEFT JOIN subgrupo_tge sgtge
                ON sgtge.idTrabajoGraduacion = tg.idTrabajoGraduacion
            WHERE CAST(u.idUsuario AS TEXT) = ?
               OR UPPER(COALESCE(u.carnetUsuario, '')) = UPPER(?)
            LIMIT 1
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(idSesion.trim(), idSesion.trim()))
        var resultado: DashboardAlumnoModel? = null

        if (cursor.moveToFirst()) {
            resultado = DashboardAlumnoModel(
                nombreCompleto = cursor.getString(cursor.getColumnIndexOrThrow("nombreCompleto")),
                carnet = cursor.getString(cursor.getColumnIndexOrThrow("carnet")),
                modalidadActual = cursor.getString(cursor.getColumnIndexOrThrow("modalidadActual")),
                estadoSolicitud = cursor.getString(cursor.getColumnIndexOrThrow("estadoSolicitud")),
                nombreTrabajo = cursor.getString(cursor.getColumnIndexOrThrow("nombreTrabajo")),
                codigoAgrupacion = cursor.getString(cursor.getColumnIndexOrThrow("codigoAgrupacion")),
                totalDocumentos = cursor.getInt(cursor.getColumnIndexOrThrow("totalDocumentos")),
                totalPropuestas = cursor.getInt(cursor.getColumnIndexOrThrow("totalPropuestas")),
                totalNotasRegistradas = cursor.getInt(cursor.getColumnIndexOrThrow("totalNotasRegistradas")),
                promedioNotas = cursor.getDouble(cursor.getColumnIndexOrThrow("promedioNotas"))
            )
        }

        cursor.close()
        return resultado
    }
}