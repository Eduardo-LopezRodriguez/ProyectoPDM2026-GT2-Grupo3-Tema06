package com.example.gradues.data.dao

import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.TrabajoDisponibleAlumnoModel
import android.content.ContentValues
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    fun aplicarATrabajo(idSesion: String, trabajo: TrabajoDisponibleAlumnoModel): Boolean {
        val db = dbHelper.writableDatabase

        db.beginTransaction()
        return try {
            val idUsuario = obtenerIdUsuarioPorSesion(idSesion, db)
            if (idUsuario <= 0) {
                db.endTransaction()
                return false
            }

            val yaTieneTrabajo = alumnoYaTieneTrabajo(idUsuario, db)
            if (yaTieneTrabajo) {
                db.endTransaction()
                return false
            }

            val yaTieneSolicitudPendiente = alumnoYaTieneSolicitudPendiente(idUsuario, db)
            if (yaTieneSolicitudPendiente) {
                db.endTransaction()
                return false
            }

            val datosTrabajo = obtenerDatosTrabajoParaSolicitud(trabajo.idTrabajoGraduacion, db)
                ?: run {
                    db.endTransaction()
                    return false
                }

            val fechaActual = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val values = ContentValues().apply {
                put("idUsuario", idUsuario)
                put("idModalidad", datosTrabajo.idModalidad)
                put("idTrabajoGraduacion", trabajo.idTrabajoGraduacion)
                put("codigoAgrupacionSolicitud", datosTrabajo.codigoAgrupacionSolicitud)
                put("nombreTrabajoPropuesto", trabajo.nombreTrabajo)
                put("fechaSolicitud", fechaActual)
                put("estadoSolicitud", "Pendiente")
                put("observacionSolicitud", "Solicitud generada desde pantalla de aplicar a trabajo")

                if (datosTrabajo.idGrupoTGESolicitado != null) {
                    put("idGrupoTGESolicitado", datosTrabajo.idGrupoTGESolicitado)
                }

                if (datosTrabajo.idEmpresaSolicitada != null) {
                    put("idEmpresaSolicitada", datosTrabajo.idEmpresaSolicitada)
                }
            }

            val resultado = db.insert("solicitud_modalidad", null, values)
            if (resultado == -1L) {
                db.endTransaction()
                return false
            }

            db.setTransactionSuccessful()
            true
        } catch (e: Exception) {
            false
        } finally {
            db.endTransaction()
        }
    }

    private fun obtenerIdUsuarioPorSesion(idSesion: String, db: android.database.sqlite.SQLiteDatabase): Int {
        val query = """
        SELECT idUsuario
        FROM usuario
        WHERE CAST(idUsuario AS TEXT) = ?
           OR UPPER(COALESCE(carnetUsuario, '')) = UPPER(?)
        LIMIT 1
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(idSesion.trim(), idSesion.trim()))
        var idUsuario = -1

        if (cursor.moveToFirst()) {
            idUsuario = cursor.getInt(cursor.getColumnIndexOrThrow("idUsuario"))
        }

        cursor.close()
        return idUsuario
    }

    private fun alumnoYaTieneTrabajo(idUsuario: Int, db: android.database.sqlite.SQLiteDatabase): Boolean {
        val query = """
        SELECT COUNT(*) AS total
        FROM alumno_trabajo
        WHERE idUsuario = ?
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(idUsuario.toString()))
        var total = 0

        if (cursor.moveToFirst()) {
            total = cursor.getInt(cursor.getColumnIndexOrThrow("total"))
        }

        cursor.close()
        return total > 0
    }

    private fun alumnoYaTieneSolicitudPendiente(idUsuario: Int, db: android.database.sqlite.SQLiteDatabase): Boolean {
        val query = """
        SELECT COUNT(*) AS total
        FROM solicitud_modalidad
        WHERE idUsuario = ?
          AND UPPER(COALESCE(estadoSolicitud, '')) = 'PENDIENTE'
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(idUsuario.toString()))
        var total = 0

        if (cursor.moveToFirst()) {
            total = cursor.getInt(cursor.getColumnIndexOrThrow("total"))
        }

        cursor.close()
        return total > 0
    }

    private fun obtenerDatosTrabajoParaSolicitud(
        idTrabajoGraduacion: Int,
        db: android.database.sqlite.SQLiteDatabase
    ): DatosTrabajoSolicitud? {
        val query = """
        SELECT
            tg.idModalidad,
            gtgi.codigoGrupoTGI,
            sgtge.idGrupoTGE,
            pp.idEmpresa
        FROM trabajo_graduacion tg
        LEFT JOIN grupo_tgi gtgi
            ON gtgi.idTrabajoGraduacion = tg.idTrabajoGraduacion
        LEFT JOIN subgrupo_tge sgtge
            ON sgtge.idTrabajoGraduacion = tg.idTrabajoGraduacion
        LEFT JOIN proyecto_pasantia pp
            ON pp.idTrabajoGraduacion = tg.idTrabajoGraduacion
        WHERE tg.idTrabajoGraduacion = ?
        LIMIT 1
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(idTrabajoGraduacion.toString()))
        var datos: DatosTrabajoSolicitud? = null

        if (cursor.moveToFirst()) {
            val idModalidad = cursor.getInt(cursor.getColumnIndexOrThrow("idModalidad"))

            val codigoAgrupacionSolicitud =
                if (cursor.isNull(cursor.getColumnIndexOrThrow("codigoGrupoTGI"))) null
                else cursor.getString(cursor.getColumnIndexOrThrow("codigoGrupoTGI"))

            val idGrupoTGESolicitado =
                if (cursor.isNull(cursor.getColumnIndexOrThrow("idGrupoTGE"))) null
                else cursor.getInt(cursor.getColumnIndexOrThrow("idGrupoTGE"))

            val idEmpresaSolicitada =
                if (cursor.isNull(cursor.getColumnIndexOrThrow("idEmpresa"))) null
                else cursor.getInt(cursor.getColumnIndexOrThrow("idEmpresa"))

            datos = DatosTrabajoSolicitud(
                idModalidad = idModalidad,
                codigoAgrupacionSolicitud = codigoAgrupacionSolicitud,
                idGrupoTGESolicitado = idGrupoTGESolicitado,
                idEmpresaSolicitada = idEmpresaSolicitada
            )
        }

        cursor.close()
        return datos
    }

    private data class DatosTrabajoSolicitud(
        val idModalidad: Int,
        val codigoAgrupacionSolicitud: String?,
        val idGrupoTGESolicitado: Int?,
        val idEmpresaSolicitada: Int?
    )
}