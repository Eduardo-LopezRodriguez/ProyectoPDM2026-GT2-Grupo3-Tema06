package com.example.gradues.data.dao

import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.TrabajoDisponibleAlumnoModel

class TrabajoDisponibleAlumnoDao(private val dbHelper: DatabaseHelper) {

    fun obtenerTrabajosDisponibles(): List<TrabajoDisponibleAlumnoModel> {
        val db = dbHelper.readableDatabase
        val lista = mutableListOf<TrabajoDisponibleAlumnoModel>()

        val query = """
            SELECT
                tg.idTrabajoGraduacion,
                tg.nombreTrabajo,
                COALESCE(m.tipoModalidad, 'Sin modalidad') AS modalidad,
                COALESCE(tg.cicloAcademico, 'Sin ciclo') AS cicloAcademico,
                COALESCE(u.nombreUsuario, 'Docente no asignado') AS docenteResponsable,
                COALESCE(tg.estadoTrabajo, 'Sin estado') AS estadoTrabajo,
                COUNT(at.idAlumnoTrabajo) AS totalAlumnos,
                COALESCE(gtgi.codigoGrupoTGI, '') AS codigoGrupoTGI,
                COALESCE(gte.nombreCurso, '') AS nombreCurso,
                COALESCE(emp.nombreEmpresa, '') AS nombreEmpresa
            FROM trabajo_graduacion tg
            LEFT JOIN modalidad m
                ON m.idModalidad = tg.idModalidad
            LEFT JOIN usuario u
                ON u.idUsuario = tg.idDocenteResponsable
            LEFT JOIN alumno_trabajo at
                ON at.idTrabajoGraduacion = tg.idTrabajoGraduacion
            LEFT JOIN grupo_tgi gtgi
                ON gtgi.idTrabajoGraduacion = tg.idTrabajoGraduacion
            LEFT JOIN subgrupo_tge sgtge
                ON sgtge.idTrabajoGraduacion = tg.idTrabajoGraduacion
            LEFT JOIN grupo_tge gte
                ON gte.idGrupoTGE = sgtge.idGrupoTGE
            LEFT JOIN proyecto_pasantia pp
                ON pp.idTrabajoGraduacion = tg.idTrabajoGraduacion
            LEFT JOIN empresa emp
                ON emp.idEmpresa = pp.idEmpresa
            WHERE UPPER(COALESCE(tg.estadoTrabajo, '')) = 'ACTIVO'
            GROUP BY
                tg.idTrabajoGraduacion,
                tg.nombreTrabajo,
                m.tipoModalidad,
                tg.cicloAcademico,
                u.nombreUsuario,
                tg.estadoTrabajo,
                gtgi.codigoGrupoTGI,
                gte.nombreCurso,
                emp.nombreEmpresa
            ORDER BY tg.idTrabajoGraduacion ASC
        """.trimIndent()

        val cursor = db.rawQuery(query, null)

        while (cursor.moveToNext()) {
            val idTrabajoGraduacion = cursor.getInt(cursor.getColumnIndexOrThrow("idTrabajoGraduacion"))
            val nombreTrabajo = cursor.getString(cursor.getColumnIndexOrThrow("nombreTrabajo"))
            val modalidad = cursor.getString(cursor.getColumnIndexOrThrow("modalidad"))
            val cicloAcademico = cursor.getString(cursor.getColumnIndexOrThrow("cicloAcademico"))
            val docenteResponsable = cursor.getString(cursor.getColumnIndexOrThrow("docenteResponsable"))
            val estadoTrabajo = cursor.getString(cursor.getColumnIndexOrThrow("estadoTrabajo"))
            val totalAlumnos = cursor.getInt(cursor.getColumnIndexOrThrow("totalAlumnos"))
            val codigoGrupoTGI = cursor.getString(cursor.getColumnIndexOrThrow("codigoGrupoTGI"))
            val nombreCurso = cursor.getString(cursor.getColumnIndexOrThrow("nombreCurso"))
            val nombreEmpresa = cursor.getString(cursor.getColumnIndexOrThrow("nombreEmpresa"))

            val cuposDisponiblesTexto = when (modalidad.trim()) {
                "Investigación", "Investigacion" -> {
                    val maximo = 5
                    val disponibles = (maximo - totalAlumnos).coerceAtLeast(0)
                    "$disponibles cupos disponibles"
                }
                "Curso de especialización", "Curso de especializacion" -> {
                    val maximo = 5
                    val disponibles = (maximo - totalAlumnos).coerceAtLeast(0)
                    "$disponibles cupos disponibles"
                }
                "Pasantía profesional", "Pasantia profesional" -> {
                    if (totalAlumnos == 0) "Disponible" else "Asignada"
                }
                else -> {
                    "Disponibilidad no definida"
                }
            }

            val descripcionSecundaria = when (modalidad.trim()) {
                "Investigación", "Investigacion" -> {
                    if (codigoGrupoTGI.isNotBlank()) {
                        "Código de grupo: $codigoGrupoTGI"
                    } else {
                        "Trabajo de investigación"
                    }
                }
                "Curso de especialización", "Curso de especializacion" -> {
                    if (nombreCurso.isNotBlank()) {
                        "Curso: $nombreCurso"
                    } else {
                        "Trabajo de especialización"
                    }
                }
                "Pasantía profesional", "Pasantia profesional" -> {
                    if (nombreEmpresa.isNotBlank()) {
                        "Empresa: $nombreEmpresa"
                    } else {
                        "Proyecto de pasantía"
                    }
                }
                else -> {
                    "Información adicional no disponible"
                }
            }

            lista.add(
                TrabajoDisponibleAlumnoModel(
                    idTrabajoGraduacion = idTrabajoGraduacion,
                    nombreTrabajo = nombreTrabajo,
                    modalidad = modalidad,
                    cicloAcademico = cicloAcademico,
                    docenteResponsable = docenteResponsable,
                    estadoTrabajo = estadoTrabajo,
                    cuposDisponiblesTexto = cuposDisponiblesTexto,
                    descripcionSecundaria = descripcionSecundaria
                )
            )
        }

        cursor.close()
        return lista
    }
}