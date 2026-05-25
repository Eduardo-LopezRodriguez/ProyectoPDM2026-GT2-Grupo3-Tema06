package com.example.gradues.data.dao

import android.database.Cursor
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.TrabajoGraduacionAdminModel

class TrabajoGraduacionAdminDao(
    private val dbHelper: DatabaseHelper
) {

    fun listarTrabajosGraduacion(): List<TrabajoGraduacionAdminModel> {
        val db = dbHelper.readableDatabase
        val lista = mutableListOf<TrabajoGraduacionAdminModel>()

        val cursor = db.rawQuery(
            """
            SELECT
                tg.idTrabajoGraduacion AS IdTrabajoGraduacion,
                COALESCE(tg.nombreTrabajo, 'Sin nombre') AS NombreTrabajo,
                COALESCE(m.tipoModalidad, 'Sin modalidad') AS Modalidad,
                COALESCE(doc.nombreUsuario, 'Sin docente asignado') AS DocenteResponsable,
                COALESCE(tg.cicloAcademico, 'Sin ciclo') AS CicloAcademico,
                COALESCE(tg.estadoTrabajo, 'Sin estado') AS EstadoTrabajo,
                COALESCE(tg.fechaInicioTrabajo, 'Sin fecha') AS FechaInicioTrabajo,
                tg.fechaFinalTrabajo AS FechaFinalTrabajo,

                CASE
                    WHEN tg.idModalidad = 1 THEN
                        'Grupo TGI: ' || COALESCE(gtgi.codigoGrupoTGI, 'Sin código')
                    WHEN tg.idModalidad = 2 THEN
                        'Curso: ' || COALESCE(gtge.nombreCurso, 'Sin curso') ||
                        CHAR(10) ||
                        'Subgrupo: ' || COALESCE(stge.nombreSubgrupo, 'Sin subgrupo') ||
                        CHAR(10) ||
                        'Tema: ' || COALESCE(stge.temaAsignado, 'Sin tema')
                    WHEN tg.idModalidad = 3 THEN
                        'Empresa: ' || COALESCE(emp.nombreEmpresa, 'Sin empresa')
                    ELSE
                        'Sin detalle de modalidad'
                END AS DetalleModalidad,

                COUNT(DISTINCT at.idUsuario) AS TotalIntegrantes,

                COALESCE(
                    GROUP_CONCAT(
                        DISTINCT COALESCE(alumno.carnetUsuario, CAST(alumno.idUsuario AS TEXT)) ||
                        ' - ' ||
                        COALESCE(alumno.nombreUsuario, 'Sin nombre')
                    ),
                    'Sin integrantes asignados'
                ) AS Integrantes

            FROM trabajo_graduacion tg

            INNER JOIN modalidad m
                ON m.idModalidad = tg.idModalidad

            LEFT JOIN usuario doc
                ON doc.idUsuario = tg.idDocenteResponsable

            LEFT JOIN alumno_trabajo at
                ON at.idTrabajoGraduacion = tg.idTrabajoGraduacion

            LEFT JOIN usuario alumno
                ON alumno.idUsuario = at.idUsuario

            LEFT JOIN grupo_tgi gtgi
                ON gtgi.idTrabajoGraduacion = tg.idTrabajoGraduacion

            LEFT JOIN subgrupo_tge stge
                ON stge.idTrabajoGraduacion = tg.idTrabajoGraduacion

            LEFT JOIN grupo_tge gtge
                ON gtge.idGrupoTGE = stge.idGrupoTGE

            LEFT JOIN proyecto_pasantia pp
                ON pp.idTrabajoGraduacion = tg.idTrabajoGraduacion

            LEFT JOIN empresa emp
                ON emp.idEmpresa = pp.idEmpresa

            GROUP BY
                tg.idTrabajoGraduacion,
                tg.nombreTrabajo,
                m.tipoModalidad,
                doc.nombreUsuario,
                tg.cicloAcademico,
                tg.estadoTrabajo,
                tg.fechaInicioTrabajo,
                tg.fechaFinalTrabajo,
                tg.idModalidad,
                gtgi.codigoGrupoTGI,
                gtge.nombreCurso,
                stge.nombreSubgrupo,
                stge.temaAsignado,
                emp.nombreEmpresa

            ORDER BY
                tg.idTrabajoGraduacion DESC
            """.trimIndent(),
            null
        )

        while (cursor.moveToNext()) {
            lista.add(
                TrabajoGraduacionAdminModel(
                    idTrabajoGraduacion = cursor.getInt(cursor.getColumnIndexOrThrow("IdTrabajoGraduacion")),
                    nombreTrabajo = getString(cursor, "NombreTrabajo"),
                    modalidad = getString(cursor, "Modalidad"),
                    docenteResponsable = getString(cursor, "DocenteResponsable"),
                    cicloAcademico = getString(cursor, "CicloAcademico"),
                    estadoTrabajo = getString(cursor, "EstadoTrabajo"),
                    fechaInicioTrabajo = getString(cursor, "FechaInicioTrabajo"),
                    fechaFinalTrabajo = getNullableString(cursor, "FechaFinalTrabajo"),
                    detalleModalidad = getString(cursor, "DetalleModalidad"),
                    totalIntegrantes = cursor.getInt(cursor.getColumnIndexOrThrow("TotalIntegrantes")),
                    integrantes = getString(cursor, "Integrantes")
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
        val value = if (cursor.isNull(index)) null else cursor.getString(index)
        return value?.ifBlank { null }
    }
}