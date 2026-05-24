package com.example.gradues.data.dao

import android.database.Cursor
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.DetalleSolicitudAdminModel

class DetalleSolicitudAdminDao(
    private val dbHelper: DatabaseHelper
) {

    fun obtenerDetalleSolicitud(idReferenciaSolicitud: Int): DetalleSolicitudAdminModel? {
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            """
            SELECT
                ref.idSolicitudModalidad AS IdReferenciaSolicitud,
                ref.idModalidad AS IdModalidad,
                ref.idTrabajoGraduacion AS IdTrabajoGraduacion,
                COALESCE(ref.codigoAgrupacionSolicitud, 'SIN-CODIGO') AS CodigoAgrupacionSolicitud,
                COALESCE(m.tipoModalidad, 'Sin modalidad') AS Modalidad,
                COALESCE(ref.nombreTrabajoPropuesto, 'Sin nombre propuesto') AS NombreTrabajoPropuesto,
                gtge.nombreCurso AS CursoSolicitado,
                emp.nombreEmpresa AS EmpresaSolicitada,
                ref.estadoSolicitud AS EstadoSolicitud,
                MIN(sm.fechaSolicitud) AS FechaSolicitud,
                COALESCE(ref.observacionSolicitud, '') AS ObservacionSolicitud,
                COUNT(sm.idSolicitudModalidad) AS TotalSolicitantes,
                GROUP_CONCAT(
                    COALESCE(u.carnetUsuario, CAST(u.idUsuario AS TEXT)) || ' - ' || COALESCE(u.nombreUsuario, 'Sin nombre'),
                    CHAR(10)
                ) AS Solicitantes
            FROM solicitud_modalidad ref
            INNER JOIN solicitud_modalidad sm
                ON sm.idModalidad = ref.idModalidad
                AND COALESCE(sm.codigoAgrupacionSolicitud, '') = COALESCE(ref.codigoAgrupacionSolicitud, '')
                AND COALESCE(sm.idGrupoTGESolicitado, -1) = COALESCE(ref.idGrupoTGESolicitado, -1)
                AND COALESCE(sm.idEmpresaSolicitada, -1) = COALESCE(ref.idEmpresaSolicitada, -1)
                AND COALESCE(sm.nombreTrabajoPropuesto, '') = COALESCE(ref.nombreTrabajoPropuesto, '')
                AND sm.estadoSolicitud = ref.estadoSolicitud
            INNER JOIN usuario u
                ON u.idUsuario = sm.idUsuario
            INNER JOIN modalidad m
                ON m.idModalidad = ref.idModalidad
            LEFT JOIN grupo_tge gtge
                ON gtge.idGrupoTGE = ref.idGrupoTGESolicitado
            LEFT JOIN empresa emp
                ON emp.idEmpresa = ref.idEmpresaSolicitada
            WHERE ref.idSolicitudModalidad = ?
            GROUP BY
                ref.idSolicitudModalidad,
                ref.idModalidad,
                ref.idTrabajoGraduacion,
                ref.codigoAgrupacionSolicitud,
                m.tipoModalidad,
                ref.nombreTrabajoPropuesto,
                gtge.nombreCurso,
                emp.nombreEmpresa,
                ref.estadoSolicitud,
                ref.observacionSolicitud
            LIMIT 1
            """.trimIndent(),
            arrayOf(idReferenciaSolicitud.toString())
        )

        var detalle: DetalleSolicitudAdminModel? = null

        if (cursor.moveToFirst()) {
            detalle = DetalleSolicitudAdminModel(
                idReferenciaSolicitud = cursor.getInt(cursor.getColumnIndexOrThrow("IdReferenciaSolicitud")),
                idModalidad = cursor.getInt(cursor.getColumnIndexOrThrow("IdModalidad")),
                idTrabajoGraduacion = getNullableInt(cursor, "IdTrabajoGraduacion"),
                codigoAgrupacionSolicitud = getString(cursor, "CodigoAgrupacionSolicitud"),
                modalidad = getString(cursor, "Modalidad"),
                nombreTrabajoPropuesto = getString(cursor, "NombreTrabajoPropuesto"),
                cursoSolicitado = getNullableString(cursor, "CursoSolicitado"),
                empresaSolicitada = getNullableString(cursor, "EmpresaSolicitada"),
                estadoSolicitud = getString(cursor, "EstadoSolicitud"),
                fechaSolicitud = getString(cursor, "FechaSolicitud"),
                observacionSolicitud = getNullableString(cursor, "ObservacionSolicitud"),
                totalSolicitantes = cursor.getInt(cursor.getColumnIndexOrThrow("TotalSolicitantes")),
                solicitantes = getString(cursor, "Solicitantes")
            )
        }

        cursor.close()
        return detalle
    }

    private fun getString(cursor: Cursor, column: String): String {
        val index = cursor.getColumnIndexOrThrow(column)
        return if (cursor.isNull(index)) "" else cursor.getString(index)
    }

    private fun getNullableString(cursor: Cursor, column: String): String? {
        val index = cursor.getColumnIndexOrThrow(column)
        val value = if (cursor.isNull(index)) null else cursor.getString(index)
        return value?.ifBlank { null }
    }

    private fun getNullableInt(cursor: Cursor, column: String): Int? {
        val index = cursor.getColumnIndexOrThrow(column)
        return if (cursor.isNull(index)) null else cursor.getInt(index)
    }
}