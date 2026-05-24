package com.example.gradues.data.dao

import android.database.Cursor
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.SolicitudModalidadAdminModel

class SolicitudModalidadAdminDao(
    private val dbHelper: DatabaseHelper
) {

    fun listarSolicitudesAgrupadas(): List<SolicitudModalidadAdminModel> {
        val db = dbHelper.readableDatabase
        val lista = mutableListOf<SolicitudModalidadAdminModel>()

        val cursor = db.rawQuery(
            """
            SELECT
                MIN(sm.idSolicitudModalidad) AS IdReferenciaSolicitud,
                sm.idModalidad AS IdModalidad,
                COALESCE(sm.codigoAgrupacionSolicitud, 'SIN-CODIGO') AS CodigoAgrupacionSolicitud,
                COALESCE(m.tipoModalidad, 'Sin modalidad') AS Modalidad,
                COALESCE(sm.nombreTrabajoPropuesto, 'Sin nombre propuesto') AS NombreTrabajoPropuesto,
                gtge.nombreCurso AS CursoSolicitado,
                emp.nombreEmpresa AS EmpresaSolicitada,
                sm.estadoSolicitud AS EstadoSolicitud,
                MIN(sm.fechaSolicitud) AS FechaSolicitud,
                COALESCE(sm.observacionSolicitud, '') AS ObservacionSolicitud,
                COUNT(sm.idSolicitudModalidad) AS TotalSolicitantes,
                GROUP_CONCAT(
                    COALESCE(u.carnetUsuario, CAST(u.idUsuario AS TEXT)) || ' - ' || COALESCE(u.nombreUsuario, 'Sin nombre'),
                    CHAR(10)
                ) AS Solicitantes
            FROM solicitud_modalidad sm
            INNER JOIN usuario u
                ON u.idUsuario = sm.idUsuario
            INNER JOIN modalidad m
                ON m.idModalidad = sm.idModalidad
            LEFT JOIN grupo_tge gtge
                ON gtge.idGrupoTGE = sm.idGrupoTGESolicitado
            LEFT JOIN empresa emp
                ON emp.idEmpresa = sm.idEmpresaSolicitada
            GROUP BY
                sm.idModalidad,
                sm.codigoAgrupacionSolicitud,
                sm.idGrupoTGESolicitado,
                sm.idEmpresaSolicitada,
                sm.nombreTrabajoPropuesto,
                sm.estadoSolicitud
            ORDER BY
                CASE sm.estadoSolicitud
                    WHEN 'Pendiente' THEN 1
                    WHEN 'Con observación' THEN 2
                    WHEN 'Aprobada' THEN 3
                    WHEN 'Rechazada' THEN 4
                    ELSE 5
                END,
                sm.idModalidad,
                sm.codigoAgrupacionSolicitud
            """.trimIndent(),
            null
        )

        while (cursor.moveToNext()) {
            lista.add(
                SolicitudModalidadAdminModel(
                    idReferenciaSolicitud = cursor.getInt(cursor.getColumnIndexOrThrow("IdReferenciaSolicitud")),
                    idModalidad = cursor.getInt(cursor.getColumnIndexOrThrow("IdModalidad")),
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
            )
        }

        cursor.close()
        return lista
    }

    private fun getString(cursor: Cursor, column: String): String {
        val index = cursor.getColumnIndexOrThrow(column)
        return if (cursor.isNull(index)) "" else cursor.getString(index)
    }

    private fun getNullableString(cursor: Cursor, column: String): String? {
        val index = cursor.getColumnIndexOrThrow(column)
        return if (cursor.isNull(index)) null else cursor.getString(index)
    }
}