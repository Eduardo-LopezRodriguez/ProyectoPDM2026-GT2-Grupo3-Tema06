package com.example.gradues.data.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.DetalleSolicitudAdminModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ResultadoOperacionAdmin {
    var ultimoError: String = ""
}

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

    fun aprobarSolicitudAgrupada(idReferenciaSolicitud: Int): Boolean {
        val db = dbHelper.writableDatabase
        db.beginTransaction()

        return try {
            val referencia = obtenerReferenciaSolicitud(db, idReferenciaSolicitud)
                ?: throw IllegalStateException("No se encontró la solicitud de referencia.")

            if (!referencia.estadoSolicitud.trim().equals("Pendiente", ignoreCase = true)) {
                throw IllegalStateException("La solicitud no está pendiente.")
            }

            validarDatosParaAprobacion(referencia)

            val solicitudes = obtenerSolicitudesPendientesRelacionadas(db, referencia)
                .ifEmpty {
                    listOf(
                        SolicitudPendiente(
                            idSolicitudModalidad = referencia.idSolicitudModalidad,
                            idUsuario = referencia.idUsuario
                        )
                    )
                }

            val idDocenteResponsable = obtenerPrimerDocenteActivo(db)
            val idTrabajoGraduacion = crearTrabajoGraduacion(db, referencia, idDocenteResponsable)


            when (referencia.idModalidad) {
                1 -> crearGrupoTGI(db, referencia, idTrabajoGraduacion)
                2 -> crearSubgrupoTGE(db, referencia, idTrabajoGraduacion)
                3 -> crearProyectoPasantia(db, referencia, idTrabajoGraduacion)
                else -> throw IllegalStateException("Modalidad no reconocida.")
            }

            solicitudes.forEach { solicitud ->
                crearAlumnoTrabajoSiNoExiste(
                    db = db,
                    idUsuario = solicitud.idUsuario,
                    idTrabajoGraduacion = idTrabajoGraduacion
                )
            }

            actualizarSolicitudesAprobadas(
                db = db,
                idsSolicitud = solicitudes.map { it.idSolicitudModalidad },
                idTrabajoGraduacion = idTrabajoGraduacion
            )

            db.setTransactionSuccessful()
            true
        } catch (e: Exception) {
            ResultadoOperacionAdmin.ultimoError = e.message ?: "Error desconocido al aprobar."
            Log.e("DetalleSolicitudAdminDao", "Error al aprobar solicitud", e)
            false
        } finally {
            db.endTransaction()
        }
    }

    fun rechazarSolicitudAgrupada(idReferenciaSolicitud: Int, observacion: String): Boolean {
        val db = dbHelper.writableDatabase
        db.beginTransaction()

        return try {
            val referencia = obtenerReferenciaSolicitud(db, idReferenciaSolicitud)
                ?: throw IllegalStateException("No se encontró la solicitud de referencia.")

            if (!referencia.estadoSolicitud.trim().equals("Pendiente", ignoreCase = true)) {
                throw IllegalStateException("La solicitud no está pendiente.")
            }

            val solicitudes = obtenerSolicitudesPendientesRelacionadas(db, referencia)
                .ifEmpty {
                    listOf(
                        SolicitudPendiente(
                            idSolicitudModalidad = referencia.idSolicitudModalidad,
                            idUsuario = referencia.idUsuario
                        )
                    )
                }

            val values = ContentValues().apply {
                put("estadoSolicitud", "Rechazada")
                put(
                    "observacionSolicitud",
                    observacion.ifBlank { "Solicitud rechazada por administración." }
                )
            }

            val placeholders = solicitudes.joinToString(",") { "?" }
            val args = solicitudes.map { it.idSolicitudModalidad.toString() }.toTypedArray()

            val filas = db.update(
                "solicitud_modalidad",
                values,
                "idSolicitudModalidad IN ($placeholders)",
                args
            )

            if (filas <= 0) {
                throw IllegalStateException("No se actualizó ninguna solicitud.")
            }

            db.setTransactionSuccessful()
            true
        } catch (e: Exception) {
            ResultadoOperacionAdmin.ultimoError = e.message ?: "Error desconocido al rechazar."
            Log.e("DetalleSolicitudAdminDao", "Error al rechazar solicitud", e)
            false
        } finally {
            db.endTransaction()
        }
    }

    private fun validarDatosParaAprobacion(referencia: SolicitudReferencia) {
        if (referencia.nombreTrabajoPropuesto.isNullOrBlank()) {
            throw IllegalStateException("Falta el nombre del trabajo propuesto.")
        }

        when (referencia.idModalidad) {
            1 -> {
                if (referencia.codigoAgrupacionSolicitud.isNullOrBlank()) {
                    throw IllegalStateException("Falta el código de grupo TGI.")
                }
            }

            2 -> {
                if (referencia.idGrupoTGESolicitado == null) {
                    throw IllegalStateException("Falta el curso de especialización solicitado.")
                }

                if (referencia.codigoAgrupacionSolicitud.isNullOrBlank()) {
                    throw IllegalStateException("Falta el código del subgrupo.")
                }
            }

            3 -> {
                if (referencia.idEmpresaSolicitada == null) {
                    throw IllegalStateException("Falta la empresa solicitada para la pasantía.")
                }
            }

            else -> {
                throw IllegalStateException("Modalidad no reconocida.")
            }
        }
    }

    private fun obtenerReferenciaSolicitud(
        db: SQLiteDatabase,
        idReferenciaSolicitud: Int
    ): SolicitudReferencia? {
        val cursor = db.rawQuery(
            """
            SELECT
                idSolicitudModalidad,
                idUsuario,
                idModalidad,
                idTrabajoGraduacion,
                idGrupoTGESolicitado,
                idEmpresaSolicitada,
                codigoAgrupacionSolicitud,
                nombreTrabajoPropuesto,
                estadoSolicitud
            FROM solicitud_modalidad
            WHERE idSolicitudModalidad = ?
            LIMIT 1
            """.trimIndent(),
            arrayOf(idReferenciaSolicitud.toString())
        )

        var referencia: SolicitudReferencia? = null

        if (cursor.moveToFirst()) {
            referencia = SolicitudReferencia(
                idSolicitudModalidad = cursor.getInt(cursor.getColumnIndexOrThrow("idSolicitudModalidad")),
                idUsuario = cursor.getInt(cursor.getColumnIndexOrThrow("idUsuario")),
                idModalidad = cursor.getInt(cursor.getColumnIndexOrThrow("idModalidad")),
                idTrabajoGraduacion = getNullableInt(cursor, "idTrabajoGraduacion"),
                idGrupoTGESolicitado = getNullableInt(cursor, "idGrupoTGESolicitado"),
                idEmpresaSolicitada = getNullableInt(cursor, "idEmpresaSolicitada"),
                codigoAgrupacionSolicitud = getNullableString(cursor, "codigoAgrupacionSolicitud"),
                nombreTrabajoPropuesto = getNullableString(cursor, "nombreTrabajoPropuesto"),
                estadoSolicitud = getString(cursor, "estadoSolicitud")
            )
        }

        cursor.close()
        return referencia
    }

    private fun obtenerSolicitudesPendientesRelacionadas(
        db: SQLiteDatabase,
        referencia: SolicitudReferencia
    ): List<SolicitudPendiente> {
        val lista = mutableListOf<SolicitudPendiente>()

        val cursor = db.rawQuery(
            """
            SELECT
                idSolicitudModalidad,
                idUsuario
            FROM solicitud_modalidad
            WHERE idModalidad = ?
              AND COALESCE(codigoAgrupacionSolicitud, '') = ?
              AND COALESCE(idGrupoTGESolicitado, -1) = ?
              AND COALESCE(idEmpresaSolicitada, -1) = ?
              AND COALESCE(nombreTrabajoPropuesto, '') = ?
              AND UPPER(TRIM(estadoSolicitud)) = 'PENDIENTE'
            """.trimIndent(),
            arrayOf(
                referencia.idModalidad.toString(),
                referencia.codigoAgrupacionSolicitud.orEmpty(),
                (referencia.idGrupoTGESolicitado ?: -1).toString(),
                (referencia.idEmpresaSolicitada ?: -1).toString(),
                referencia.nombreTrabajoPropuesto.orEmpty()
            )
        )

        while (cursor.moveToNext()) {
            lista.add(
                SolicitudPendiente(
                    idSolicitudModalidad = cursor.getInt(cursor.getColumnIndexOrThrow("idSolicitudModalidad")),
                    idUsuario = cursor.getInt(cursor.getColumnIndexOrThrow("idUsuario"))
                )
            )
        }

        cursor.close()
        return lista
    }

    private fun crearTrabajoGraduacion(
        db: SQLiteDatabase,
        referencia: SolicitudReferencia,
        idDocenteResponsable: Int?
    ): Int {
        val values = ContentValues().apply {
            put("idModalidad", referencia.idModalidad)

            if (idDocenteResponsable != null) {
                put("idDocenteResponsable", idDocenteResponsable)
            } else {
                putNull("idDocenteResponsable")
            }

            put(
                "nombreTrabajo",
                referencia.nombreTrabajoPropuesto ?: "Trabajo de graduación"
            )
            put("cicloAcademico", "Ciclo I")
            put("estadoTrabajo", "Activo")
            put("fechaInicioTrabajo", fechaActual())
            putNull("fechaFinalTrabajo")
        }

        val id = db.insert("trabajo_graduacion", null, values)

        if (id == -1L) {
            throw IllegalStateException("No se pudo crear trabajo_graduacion.")
        }

        return id.toInt()
    }

    private fun crearGrupoTGI(
        db: SQLiteDatabase,
        referencia: SolicitudReferencia,
        idTrabajoGraduacion: Int
    ) {
        val values = ContentValues().apply {
            put("idTrabajoGraduacion", idTrabajoGraduacion)
            put(
                "codigoGrupoTGI",
                referencia.codigoAgrupacionSolicitud ?: "TGI-$idTrabajoGraduacion"
            )
            put("fechaCreacion", fechaActual())
            putNull("fechaFinal")
        }

        val id = db.insert("grupo_tgi", null, values)

        if (id == -1L) {
            throw IllegalStateException("No se pudo crear grupo_tgi.")
        }
    }

    private fun crearSubgrupoTGE(
        db: SQLiteDatabase,
        referencia: SolicitudReferencia,
        idTrabajoGraduacion: Int
    ) {
        val idGrupoTGE = referencia.idGrupoTGESolicitado
            ?: throw IllegalStateException("La solicitud de especialización no tiene grupo TGE.")

        val values = ContentValues().apply {
            put("idGrupoTGE", idGrupoTGE)
            put("idTrabajoGraduacion", idTrabajoGraduacion)
            put(
                "nombreSubgrupo",
                referencia.codigoAgrupacionSolicitud ?: "SUB-$idTrabajoGraduacion"
            )
            put(
                "temaAsignado",
                referencia.nombreTrabajoPropuesto ?: "Tema asignado"
            )
        }

        val id = db.insert("subgrupo_tge", null, values)

        if (id == -1L) {
            throw IllegalStateException("No se pudo crear subgrupo_tge.")
        }
    }

    private fun crearProyectoPasantia(
        db: SQLiteDatabase,
        referencia: SolicitudReferencia,
        idTrabajoGraduacion: Int
    ) {
        val idEmpresa = referencia.idEmpresaSolicitada
            ?: throw IllegalStateException("La solicitud de pasantía no tiene empresa.")

        val values = ContentValues().apply {
            put("idTrabajoGraduacion", idTrabajoGraduacion)
            put("idEmpresa", idEmpresa)
            put("fechaInicioPasantia", fechaActual())
            putNull("fechaFinalPasantia")
            put("estadoPasantia", "Activo")
        }

        val id = db.insert("proyecto_pasantia", null, values)

        if (id == -1L) {
            throw IllegalStateException("No se pudo crear proyecto_pasantia.")
        }
    }

    private fun crearAlumnoTrabajoSiNoExiste(
        db: SQLiteDatabase,
        idUsuario: Int,
        idTrabajoGraduacion: Int
    ) {
        val cursor = db.rawQuery(
            """
            SELECT COUNT(*) AS Total
            FROM alumno_trabajo
            WHERE idUsuario = ?
              AND idTrabajoGraduacion = ?
            """.trimIndent(),
            arrayOf(idUsuario.toString(), idTrabajoGraduacion.toString())
        )

        var existe = false

        if (cursor.moveToFirst()) {
            existe = cursor.getInt(cursor.getColumnIndexOrThrow("Total")) > 0
        }

        cursor.close()

        if (existe) return

        val values = ContentValues().apply {
            put("idUsuario", idUsuario)
            put("idTrabajoGraduacion", idTrabajoGraduacion)
            put("estadoAlumnoTrabajo", "Activo")
        }

        val id = db.insert("alumno_trabajo", null, values)

        if (id == -1L) {
            throw IllegalStateException("No se pudo crear alumno_trabajo.")
        }
    }

    private fun actualizarSolicitudesAprobadas(
        db: SQLiteDatabase,
        idsSolicitud: List<Int>,
        idTrabajoGraduacion: Int
    ) {
        val values = ContentValues().apply {
            put("idTrabajoGraduacion", idTrabajoGraduacion)
            put("estadoSolicitud", "Aprobada")
            put("observacionSolicitud", "Solicitud aprobada por administración.")
        }

        val placeholders = idsSolicitud.joinToString(",") { "?" }
        val args = idsSolicitud.map { it.toString() }.toTypedArray()

        db.update(
            "solicitud_modalidad",
            values,
            "idSolicitudModalidad IN ($placeholders)",
            args
        )
    }

    private fun obtenerPrimerDocenteActivo(db: SQLiteDatabase): Int? {
        val cursor = db.rawQuery(
            """
            SELECT u.idUsuario
            FROM usuario u
            INNER JOIN rol r
                ON r.idRol = u.idRol
            WHERE r.nombreRol = 'Docente'
              AND COALESCE(u.estadoUsuario, 'Activo') = 'Activo'
            ORDER BY u.idUsuario
            LIMIT 1
            """.trimIndent(),
            null
        )

        var idDocente: Int? = null

        if (cursor.moveToFirst()) {
            idDocente = cursor.getInt(cursor.getColumnIndexOrThrow("idUsuario"))
        }

        cursor.close()
        return idDocente
    }

    private fun fechaActual(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
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

    private data class SolicitudReferencia(
        val idSolicitudModalidad: Int,
        val idUsuario: Int,
        val idModalidad: Int,
        val idTrabajoGraduacion: Int?,
        val idGrupoTGESolicitado: Int?,
        val idEmpresaSolicitada: Int?,
        val codigoAgrupacionSolicitud: String?,
        val nombreTrabajoPropuesto: String?,
        val estadoSolicitud: String
    )

    private data class SolicitudPendiente(
        val idSolicitudModalidad: Int,
        val idUsuario: Int
    )
}