package com.example.gradues.data.dao

import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.SolicitudAlumnoDetalleModel

class SolicitudAlumnoDetalleDao(private val dbHelper: DatabaseHelper) {

    fun obtenerUltimaSolicitudAlumno(idSesion: String): SolicitudAlumnoDetalleModel? {
        val db = dbHelper.readableDatabase

        val query = """
            SELECT
                sm.idSolicitudModalidad,
                COALESCE(sm.estadoSolicitud, 'Sin solicitud') AS estadoSolicitud,
                COALESCE(sm.fechaSolicitud, '') AS fechaSolicitud,
                COALESCE(m.tipoModalidad, '') AS modalidad,
                COALESCE(sm.nombreTrabajoPropuesto, '') AS nombreTrabajoPropuesto,
                COALESCE(sm.codigoAgrupacionSolicitud, '') AS codigoAgrupacionSolicitud,
                COALESCE(sm.observacionSolicitud, '') AS observacionSolicitud,
                COALESCE(tg.nombreTrabajo, '') AS nombreTrabajoAsociado,
                COALESCE(gte.nombreCurso, '') AS nombreCurso,
                COALESCE(emp.nombreEmpresa, '') AS nombreEmpresa,
                COALESCE(gtgi.codigoGrupoTGI, '') AS codigoGrupoTGI
            FROM solicitud_modalidad sm
            INNER JOIN usuario u
                ON u.idUsuario = sm.idUsuario
            LEFT JOIN modalidad m
                ON m.idModalidad = sm.idModalidad
            LEFT JOIN trabajo_graduacion tg
                ON tg.idTrabajoGraduacion = sm.idTrabajoGraduacion
            LEFT JOIN grupo_tgi gtgi
                ON gtgi.idTrabajoGraduacion = sm.idTrabajoGraduacion
            LEFT JOIN grupo_tge gte
                ON gte.idGrupoTGE = sm.idGrupoTGESolicitado
            LEFT JOIN empresa emp
                ON emp.idEmpresa = sm.idEmpresaSolicitada
            WHERE CAST(u.idUsuario AS TEXT) = ?
               OR UPPER(COALESCE(u.carnetUsuario, '')) = UPPER(?)
            ORDER BY sm.idSolicitudModalidad DESC
            LIMIT 1
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(idSesion.trim(), idSesion.trim()))

        var resultado: SolicitudAlumnoDetalleModel? = null

        if (cursor.moveToFirst()) {
            resultado = SolicitudAlumnoDetalleModel(
                idSolicitudModalidad = cursor.getInt(cursor.getColumnIndexOrThrow("idSolicitudModalidad")),
                estadoSolicitud = cursor.getString(cursor.getColumnIndexOrThrow("estadoSolicitud")),
                fechaSolicitud = cursor.getString(cursor.getColumnIndexOrThrow("fechaSolicitud")),
                modalidad = cursor.getString(cursor.getColumnIndexOrThrow("modalidad")),
                nombreTrabajoPropuesto = cursor.getString(cursor.getColumnIndexOrThrow("nombreTrabajoPropuesto")),
                codigoAgrupacionSolicitud = cursor.getString(cursor.getColumnIndexOrThrow("codigoAgrupacionSolicitud")),
                observacionSolicitud = cursor.getString(cursor.getColumnIndexOrThrow("observacionSolicitud")),
                nombreTrabajoAsociado = cursor.getString(cursor.getColumnIndexOrThrow("nombreTrabajoAsociado")),
                nombreCurso = cursor.getString(cursor.getColumnIndexOrThrow("nombreCurso")),
                nombreEmpresa = cursor.getString(cursor.getColumnIndexOrThrow("nombreEmpresa")),
                codigoGrupoTGI = cursor.getString(cursor.getColumnIndexOrThrow("codigoGrupoTGI"))
            )
        }

        cursor.close()
        return resultado
    }
}